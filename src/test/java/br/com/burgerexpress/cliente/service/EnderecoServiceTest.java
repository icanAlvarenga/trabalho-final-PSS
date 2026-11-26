package br.com.burgerexpress.cliente.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.burgerexpress.cliente.dto.EnderecoRequest;
import br.com.burgerexpress.cliente.dto.EnderecoResponse;
import br.com.burgerexpress.cliente.dto.EnderecoResumo;
import br.com.burgerexpress.cliente.entity.Cliente;
import br.com.burgerexpress.cliente.entity.Endereco;
import br.com.burgerexpress.cliente.repository.EnderecoRepository;
import br.com.burgerexpress.shared.exception.RecursoNaoEncontradoException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private ClienteLocalizador clienteLocalizador;

    @Mock
    private PoliticaEnderecoPrincipal politicaEnderecoPrincipal;

    private EnderecoService service;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        service = new EnderecoService(enderecoRepository, clienteLocalizador, new EnderecoMapperImpl(),
                politicaEnderecoPrincipal);
        cliente = new Cliente("Maria", "maria@example.com", "27999998888");
        ReflectionTestUtils.setField(cliente, "id", 1L);
    }

    private EnderecoRequest request(Boolean principal) {
        return new EnderecoRequest("29560000", "Rua A", "10", "Centro", "Guaçuí", null, principal);
    }

    private Endereco enderecoComId(Long id, boolean principal) {
        Endereco e = new Endereco(cliente, "29560000", "Rua A", "10", "Centro", "Guaçuí", null, principal);
        ReflectionTestUtils.setField(e, "id", id);
        return e;
    }

    @Test
    void criaEnderecoAssociadoAoClienteDaRota() {
        Endereco salvo = enderecoComId(7L, false);
        when(clienteLocalizador.localizar(1L)).thenReturn(cliente);
        when(enderecoRepository.saveAndFlush(any(Endereco.class))).thenReturn(salvo);

        EnderecoResponse resposta = service.criar(1L, request(null));

        assertThat(resposta.id()).isEqualTo(7L);
        assertThat(resposta.clienteId()).isEqualTo(1L);
        assertThat(resposta.principal()).isFalse();
    }

    @Test
    void criarEnderecoConsultaAPoliticaDeEnderecoPrincipalAntesDeSalvar() {
        Endereco salvo = enderecoComId(8L, true);
        when(clienteLocalizador.localizar(1L)).thenReturn(cliente);
        when(enderecoRepository.saveAndFlush(any(Endereco.class))).thenReturn(salvo);

        EnderecoResponse resposta = service.criar(1L, request(true));

        verify(politicaEnderecoPrincipal).aplicar(eq(1L), any(Endereco.class));
        assertThat(resposta.principal()).isTrue();
    }

    @Test
    void criarEnderecoParaClienteInexistenteLancaNaoEncontrado() {
        when(clienteLocalizador.localizar(99L))
                .thenThrow(new RecursoNaoEncontradoException("Cliente com id 99 não encontrado."));

        try {
            service.criar(99L, request(null));
            fail("Deveria lançar RecursoNaoEncontradoException");
        } catch (RecursoNaoEncontradoException esperado) {
            assertThat(esperado.getMessage()).contains("99");
        }
        verify(enderecoRepository, never()).saveAndFlush(any());
        verify(politicaEnderecoPrincipal, never()).aplicar(any(), any());
    }

    @Test
    void validarEnderecoPertencenteAoClienteRetornaResumoSemExporEntidade() {
        Endereco endereco = enderecoComId(5L, true);
        when(clienteLocalizador.localizar(1L)).thenReturn(cliente);
        when(enderecoRepository.findByIdAndClienteId(5L, 1L)).thenReturn(Optional.of(endereco));

        EnderecoResumo resumo = service.validarEnderecoDoCliente(1L, 5L);

        assertThat(resumo.id()).isEqualTo(5L);
        assertThat(resumo.clienteId()).isEqualTo(1L);
        assertThat(resumo.cidade()).isEqualTo("Guaçuí");
        assertThat(resumo.principal()).isTrue();
    }

    @Test
    void validarEnderecoDeOutroClienteLancaNaoEncontrado() {
        when(clienteLocalizador.localizar(1L)).thenReturn(cliente);
        when(enderecoRepository.findByIdAndClienteId(5L, 1L)).thenReturn(Optional.empty());

        try {
            service.validarEnderecoDoCliente(1L, 5L);
            fail("Deveria lançar RecursoNaoEncontradoException");
        } catch (RecursoNaoEncontradoException esperado) {
            assertThat(esperado.getMessage()).contains("Endereço");
        }
    }

    @Test
    void validarEnderecoComClienteInexistenteLancaNaoEncontrado() {
        when(clienteLocalizador.localizar(99L))
                .thenThrow(new RecursoNaoEncontradoException("Cliente com id 99 não encontrado."));

        try {
            service.validarEnderecoDoCliente(99L, 5L);
            fail("Deveria lançar RecursoNaoEncontradoException");
        } catch (RecursoNaoEncontradoException esperado) {
            assertThat(esperado.getMessage()).contains("Cliente");
        }
    }
}
