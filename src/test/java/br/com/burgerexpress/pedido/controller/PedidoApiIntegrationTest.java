package br.com.burgerexpress.pedido.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.burgerexpress.catalogo.entity.Categoria;
import br.com.burgerexpress.catalogo.entity.Produto;
import br.com.burgerexpress.catalogo.repository.CategoriaRepository;
import br.com.burgerexpress.catalogo.repository.ProdutoRepository;
import br.com.burgerexpress.cliente.entity.Cliente;
import br.com.burgerexpress.cliente.entity.Endereco;
import br.com.burgerexpress.cliente.repository.ClienteRepository;
import br.com.burgerexpress.cliente.repository.EnderecoRepository;
import br.com.burgerexpress.pedido.dto.PedidoResponse;
import br.com.burgerexpress.pedido.entity.StatusPedido;
import br.com.burgerexpress.pedido.service.PedidoConsulta;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:burgerexpress-pedidos;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class PedidoApiIntegrationTest {

    private static final String ROTA = "/api/v1/pedidos";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ClienteRepository clientes;
    @Autowired
    private EnderecoRepository enderecos;
    @Autowired
    private CategoriaRepository categorias;
    @Autowired
    private ProdutoRepository produtos;
    @Autowired
    private PedidoConsulta consulta;

    private Cliente cliente;
    private Endereco endereco;
    private Produto hamburguer;
    private Produto fritas;

    @BeforeEach
    void prepararDados() {
        jdbc.update("delete from itens_pedido");
        jdbc.update("delete from pedidos");
        jdbc.update("delete from produtos");
        jdbc.update("delete from categorias");
        jdbc.update("delete from enderecos");
        jdbc.update("delete from clientes");

        cliente = clientes.save(new Cliente("Maria", "maria@example.com", "27999998888"));
        endereco = criarEndereco(cliente);
        Categoria categoria = categorias.save(new Categoria("Lanches", null));
        hamburguer = produtos.save(new Produto("Hambúrguer", null, new BigDecimal("10.15"), 5, categoria));
        fritas = produtos.save(new Produto("Fritas", null, new BigDecimal("4.83"), 1, categoria));
    }

    @Test
    void criaPedidoComDoisProdutosCalculaTotalEGuardaPrecoHistorico() throws Exception {
        String itens = item(hamburguer, 2) + "," + item(fritas, 1);
        String corpo = enviarPedido(pedido(cliente.getId(), endereco.getId(), itens))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"))
                .andExpect(jsonPath("$.dataCriacao").isString())
                .andExpect(jsonPath("$.clienteId").value(cliente.getId()))
                .andExpect(jsonPath("$.enderecoId").value(endereco.getId()))
                .andExpect(jsonPath("$.observacoes").value("Sem cebola"))
                .andExpect(jsonPath("$.total").value(25.13))
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.itens[0].produtoId").value(hamburguer.getId()))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(10.15))
                .andExpect(jsonPath("$.itens[0].subtotal").value(20.30))
                .andExpect(jsonPath("$.itens[1].subtotal").value(4.83))
                .andReturn().getResponse().getContentAsString();
        long id = lerId(corpo);

        assertThat(estoque(hamburguer)).isEqualTo(3);
        assertThat(estoque(fritas)).isZero();
        Produto atualizado = produtos.findById(hamburguer.getId()).orElseThrow();
        atualizado.setPreco(new BigDecimal("12.00"));
        produtos.save(atualizado);

        PedidoResponse pedido = consulta.buscarPorId(id);
        assertThat(pedido.status()).isEqualTo(StatusPedido.AGUARDANDO_PAGAMENTO);
        assertThat(pedido.total()).isEqualByComparingTo("25.13");
        assertThat(pedido.itens().get(0).precoUnitario()).isEqualByComparingTo("10.15");
        assertThat(pedido.itens().get(0).subtotal()).isEqualByComparingTo("20.30");
        assertThat(pedido.itens().get(1).precoUnitario()).isEqualByComparingTo("4.83");
    }

    @Test
    void consultaPedidoEListaSomenteOsPedidosDoCliente() throws Exception {
        long primeiro = criarPedido(cliente, endereco);
        Cliente outroCliente = clientes.save(new Cliente("Pedro", "pedro@example.com", "27999998889"));
        long pedidoDeOutroCliente = criarPedido(outroCliente, criarEndereco(outroCliente));
        long segundo = criarPedido(cliente, endereco);

        mockMvc.perform(get(ROTA + "/{id}", primeiro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(primeiro))
                .andExpect(jsonPath("$.itens[0].quantidade").value(1));
        mockMvc.perform(get(ROTA + "/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
        mockMvc.perform(get(ROTA).param("clienteId", cliente.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(segundo))
                .andExpect(jsonPath("$[1].id").value(primeiro));
        mockMvc.perform(get(ROTA).param("clienteId", outroCliente.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(pedidoDeOutroCliente));
        mockMvc.perform(get(ROTA).param("clienteId", Long.toString(Long.MAX_VALUE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get(ROTA)).andExpect(status().isBadRequest());
        mockMvc.perform(get(ROTA).param("clienteId", "0")).andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaPedidoSemItens() throws Exception {
        enviarPedido(pedido(cliente.getId(), endereco.getId(), ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString("itens")));
        verificarSemPedidoOuReserva();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "1.5", "null", "2147483648"})
    void rejeitaQuantidadeInvalida(String quantidade) throws Exception {
        String item = "{\"produtoId\": %d, \"quantidade\": %s}".formatted(hamburguer.getId(), quantidade);
        enviarPedido(pedido(cliente.getId(), endereco.getId(), item)).andExpect(status().isBadRequest());
        verificarSemPedidoOuReserva();
    }

    @Test
    void rejeitaItemNuloEIdsInvalidos() throws Exception {
        enviarPedido(pedido(cliente.getId(), endereco.getId(), "null")).andExpect(status().isBadRequest());
        enviarPedido(pedido(0, endereco.getId(), item(hamburguer, 1))).andExpect(status().isBadRequest());
        enviarPedido(pedido(cliente.getId(), 0, item(hamburguer, 1))).andExpect(status().isBadRequest());
        enviarPedido(pedido(cliente.getId(), endereco.getId(), "{\"produtoId\": 0, \"quantidade\": 1}"))
                .andExpect(status().isBadRequest());
        String itemComIdFracionario = "{\"produtoId\": %d.5, \"quantidade\": 1}".formatted(hamburguer.getId());
        enviarPedido(pedido(cliente.getId(), endereco.getId(), itemComIdFracionario))
                .andExpect(status().isBadRequest());
        verificarSemPedidoOuReserva();
    }

    @Test
    void rejeitaTotalPrecoESubtotalEnviadosPeloCliente() throws Exception {
        String valido = pedido(cliente.getId(), endereco.getId(), item(hamburguer, 1));
        enviarPedido(valido.replace("\"observacoes\"", "\"total\": 0.01, \"observacoes\""))
                .andExpect(status().isBadRequest());
        for (String campo : List.of("preco", "precoUnitario", "subtotal")) {
            enviarPedido(valido.replace("\"quantidade\"", "\"" + campo + "\": 0.01, \"quantidade\""))
                    .andExpect(status().isBadRequest());
        }
        verificarSemPedidoOuReserva();
    }

    @Test
    void rejeitaClienteOuEnderecoInexistenteEEnderecoDeOutroCliente() throws Exception {
        Cliente outroCliente = clientes.save(new Cliente("Pedro", "pedro@example.com", "27999998889"));
        String item = item(hamburguer, 1);
        enviarPedido(pedido(Long.MAX_VALUE, endereco.getId(), item)).andExpect(status().isNotFound());
        enviarPedido(pedido(cliente.getId(), Long.MAX_VALUE, item)).andExpect(status().isNotFound());
        enviarPedido(pedido(outroCliente.getId(), endereco.getId(), item)).andExpect(status().isNotFound());
        verificarSemPedidoOuReserva();
    }

    @Test
    void desfazPrimeiraReservaQuandoSegundoProdutoNaoExiste() throws Exception {
        String itemInexistente = "{\"produtoId\": %d, \"quantidade\": 1}".formatted(Long.MAX_VALUE);
        enviarPedido(pedido(cliente.getId(), endereco.getId(), item(hamburguer, 2) + "," + itemInexistente))
                .andExpect(status().isNotFound());
        verificarSemPedidoOuReserva();
    }

    @Test
    void rejeitaProdutoIndisponivel() throws Exception {
        fritas.setDisponivel(false);
        produtos.save(fritas);

        enviarPedido(pedido(cliente.getId(), endereco.getId(), item(fritas, 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("indisponível")));
        verificarSemPedidoOuReserva();
    }

    @Test
    void desfazPrimeiraReservaQuandoSegundoProdutoNaoTemEstoque() throws Exception {
        String itens = item(hamburguer, 2) + "," + item(fritas, 2);
        enviarPedido(pedido(cliente.getId(), endereco.getId(), itens))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("Estoque insuficiente")));
        verificarSemPedidoOuReserva();
    }

    @Test
    void consideraQuantidadeAcumuladaQuandoProdutoSeRepete() throws Exception {
        String itens = item(hamburguer, 3) + "," + item(hamburguer, 3);
        enviarPedido(pedido(cliente.getId(), endereco.getId(), itens))
                .andExpect(status().isConflict());
        verificarSemPedidoOuReserva();
    }

    @Test
    void somenteUmPedidoReservaAUltimaUnidadeConcorrentemente() throws Exception {
        String corpo = pedido(cliente.getId(), endereco.getId(), item(fritas, 1));
        CountDownLatch prontos = new CountDownLatch(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Integer> primeiro = executor.submit(new EnvioConcorrente(corpo, prontos, inicio));
            Future<Integer> segundo = executor.submit(new EnvioConcorrente(corpo, prontos, inicio));
            boolean ambosProntos = prontos.await(10, TimeUnit.SECONDS);
            inicio.countDown();
            assertThat(ambosProntos).isTrue();
            assertThat(List.of(primeiro.get(15, TimeUnit.SECONDS), segundo.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
        }
        assertThat(estoque(fritas)).isZero();
        assertThat(consulta.listarPorCliente(cliente.getId())).hasSize(1);
    }

    @Test
    void preservaHistoricoAoTentarExcluirClienteComPedidos() throws Exception {
        long id = criarPedido(cliente, endereco);
        mockMvc.perform(delete("/api/v1/clientes/{id}", cliente.getId())).andExpect(status().isConflict());
        mockMvc.perform(get(ROTA + "/{id}", id)).andExpect(status().isOk());
    }

    private Endereco criarEndereco(Cliente dono) {
        return enderecos.save(new Endereco(dono, "29560000", "Rua A", "10", "Centro", "Guaçuí", null, true));
    }

    private long criarPedido(Cliente dono, Endereco destino) throws Exception {
        MockHttpServletResponse resposta = enviarPedido(pedido(dono.getId(), destino.getId(), item(hamburguer, 1)))
                .andExpect(status().isCreated()).andReturn().getResponse();
        long id = lerId(resposta.getContentAsString());
        assertThat(resposta.getHeader("Location")).isEqualTo(ROTA + "/" + id);
        return id;
    }

    private ResultActions enviarPedido(String corpo) throws Exception {
        return mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private long lerId(String corpo) throws Exception {
        return objectMapper.readTree(corpo).get("id").asLong();
    }

    private String pedido(long clienteId, long enderecoId, String itens) {
        return """
                {"clienteId": %d, "enderecoId": %d, "observacoes": "Sem cebola", "itens": [%s]}
                """.formatted(clienteId, enderecoId, itens);
    }

    private String item(Produto produto, int quantidade) {
        return """
                {"produtoId": %d, "quantidade": %d}
                """.strip().formatted(produto.getId(), quantidade);
    }

    private int estoque(Produto produto) {
        return produtos.findById(produto.getId()).orElseThrow().getEstoque();
    }

    private void verificarSemPedidoOuReserva() {
        assertThat(jdbc.queryForObject("select count(*) from pedidos", Long.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from itens_pedido", Long.class)).isZero();
        assertThat(estoque(hamburguer)).isEqualTo(5);
        assertThat(estoque(fritas)).isEqualTo(1);
    }

    private class EnvioConcorrente implements Callable<Integer> {

        private final String corpo;
        private final CountDownLatch prontos;
        private final CountDownLatch inicio;

        EnvioConcorrente(String corpo, CountDownLatch prontos, CountDownLatch inicio) {
            this.corpo = corpo;
            this.prontos = prontos;
            this.inicio = inicio;
        }

        @Override
        public Integer call() throws Exception {
            prontos.countDown();
            if (!inicio.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("O teste não liberou o início dos pedidos.");
            }
            return enviarPedido(corpo).andReturn().getResponse().getStatus();
        }
    }
}
