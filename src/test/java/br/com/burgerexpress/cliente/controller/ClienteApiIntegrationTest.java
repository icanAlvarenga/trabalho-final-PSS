package br.com.burgerexpress.cliente.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Testes de integração da camada REST (Controller, Service, Repository e H2). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClienteApiIntegrationTest {

    private static final String CLIENTE_VALIDO = """
            { "nome": "Maria Silva", "email": "maria@example.com", "telefone": "27999998888" }
            """;

    private static final String ENDERECO_VALIDO = """
            { "cep": "29560000", "rua": "Rua A", "numero": "10", "bairro": "Centro",
              "cidade": "Guaçuí", "complemento": "Apto 2", "principal": true }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long criarCliente(String json) throws Exception {
        String corpo = mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(corpo);
        return node.get("id").asLong();
    }

    @Test
    void cadastraClienteValidoEConsultaPorId() throws Exception {
        String corpo = mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON).content(CLIENTE_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/clientes/")))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.email").value("maria@example.com"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(corpo).get("id").asLong();

        mockMvc.perform(get("/api/v1/clientes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("maria@example.com"));

        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void emailDuplicadoRetorna409ComCorpoPadronizado() throws Exception {
        criarCliente(CLIENTE_VALIDO);

        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(CLIENTE_VALIDO.replace("maria@example.com", "MARIA@example.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflito de estado"))
                .andExpect(jsonPath("$.detail").value(containsString("e-mail")))
                .andExpect(jsonPath("$.path").value("/api/v1/clientes"));
    }

    @Test
    void emailInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(CLIENTE_VALIDO.replace("maria@example.com", "nao-e-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(containsString("email")));
    }

    @Test
    void campoObrigatorioAusenteRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"maria@example.com\", \"telefone\": \"27999998888\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("nome")));
    }

    @Test
    void jsonMalformadoRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON).content("{ nome: "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"));
    }

    @Test
    void clienteInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/clientes/9999"));

        mockMvc.perform(put("/api/v1/clientes/{id}", 9999).contentType(MediaType.APPLICATION_JSON)
                        .content(CLIENTE_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void idNaoNumericoRetorna400() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizaClienteEMantemEmailUnico() throws Exception {
        long id = criarCliente(CLIENTE_VALIDO);
        criarCliente(CLIENTE_VALIDO.replace("maria@example.com", "joao@example.com"));

        mockMvc.perform(put("/api/v1/clientes/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(CLIENTE_VALIDO.replace("Maria Silva", "Maria Souza")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Souza"));

        mockMvc.perform(put("/api/v1/clientes/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(CLIENTE_VALIDO.replace("maria@example.com", "joao@example.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void removeClienteRetorna204EDepoisConsultaRetorna404() throws Exception {
        long id = criarCliente(CLIENTE_VALIDO);

        mockMvc.perform(delete("/api/v1/clientes/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/clientes/{id}", id)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/v1/clientes/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void cadastraEListaEnderecosDoCliente() throws Exception {
        long clienteId = criarCliente(CLIENTE_VALIDO);

        mockMvc.perform(post("/api/v1/clientes/{id}/enderecos", clienteId)
                        .contentType(MediaType.APPLICATION_JSON).content(ENDERECO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(clienteId))
                .andExpect(jsonPath("$.principal").value(true))
                .andExpect(jsonPath("$.cidade").value("Guaçuí"));

        mockMvc.perform(post("/api/v1/clientes/{id}/enderecos", clienteId)
                        .contentType(MediaType.APPLICATION_JSON).content(ENDERECO_VALIDO))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/clientes/{id}/enderecos", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].principal").value(false))
                .andExpect(jsonPath("$[1].principal").value(true));
    }

    @Test
    void enderecoParaClienteInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/api/v1/clientes/{id}/enderecos", 9999)
                        .contentType(MediaType.APPLICATION_JSON).content(ENDERECO_VALIDO))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/clientes/{id}/enderecos", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    void enderecoComCepInvalidoRetorna400() throws Exception {
        long clienteId = criarCliente(CLIENTE_VALIDO);

        mockMvc.perform(post("/api/v1/clientes/{id}/enderecos", clienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ENDERECO_VALIDO.replace("29560000", "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("cep")));
    }

    @Test
    void rotaInexistenteRetorna404ComCorpoPadronizado() throws Exception {
        mockMvc.perform(get("/api/v1/nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
