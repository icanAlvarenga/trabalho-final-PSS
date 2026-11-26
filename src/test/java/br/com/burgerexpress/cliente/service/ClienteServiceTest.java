package br.com.burgerexpress.cliente.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.burgerexpress.cliente.dto.ClienteRequest;
import br.com.burgerexpress.cliente.dto.ClienteResponse;
import br.com.burgerexpress.cliente.entity.Cliente;
import br.com.burgerexpress.cliente.repository.ClienteRepository;
import br.com.burgerexpress.shared.exception.ConflitoException;
import br.com.burgerexpress.shared.exception.RecursoNaoEncontradoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    /** Restrição de exclusão com resposta fixa (classe comum, sem lambda). */
    private static class RestricaoFixa implements RestricaoExclusaoCliente {

        private final boolean impede;

        RestricaoFixa(boolean impede) {
            this.impede = impede;
        }

        @Override
        public boolean impedeExclusao(Long clienteId) {
            return impede;
        }
    }

    @Mock
    private ClienteRepository clienteRepository;

    private final ClienteMapper mapper = new ClienteMapperImpl();

    private ClienteService service(RestricaoExclusaoCliente... restricoes) {
        List<RestricaoExclusaoCliente> lista = new ArrayList<>();
        for (RestricaoExclusaoCliente restricao : restricoes) {
            lista.add(restricao);
        }
        return new ClienteService(clienteRepository, mapper, lista);
    }

    private static Cliente clienteComId(Long id) {
        Cliente c = new Cliente("Maria", "maria@example.com", "27999998888");
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    private static ClienteRequest request(String email) {
        return new ClienteRequest("  Maria Silva ", email, "27999998888", null);
    }

    @Test
    void criaClienteValidoNormalizandoEmailENomeEAtivoPadrao() {
        Cliente salvo = new Cliente("Maria Silva", "maria@example.com", "27999998888");
        ReflectionTestUtils.setField(salvo, "id", 1L);
        when(clienteRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(false);
        when(clienteRepository.saveAndFlush(any(Cliente.class))).thenReturn(salvo);

        ClienteResponse resposta = service().criar(request(" Maria@Example.com "));

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).saveAndFlush(captor.capture());
        Cliente enviadoParaSalvar = captor.getValue();
        assertThat(enviadoParaSalvar.getNome()).isEqualTo("Maria Silva");
        assertThat(enviadoParaSalvar.getEmail()).isEqualTo("maria@example.com");
        assertThat(enviadoParaSalvar.isAtivo()).isTrue();
        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.email()).isEqualTo("maria@example.com");
    }

    @Test
    void emailDuplicadoLancaConflitoESemSalvar() {
        when(clienteRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(true);

        try {
            service().criar(request("maria@example.com"));
            fail("Deveria lançar ConflitoException");
        } catch (ConflitoException esperado) {
            assertThat(esperado.getMessage()).contains("e-mail");
        }
        verify(clienteRepository, never()).saveAndFlush(any());
    }

    @Test
    void obterClienteInexistenteLancaNaoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        try {
            service().obter(99L);
            fail("Deveria lançar RecursoNaoEncontradoException");
        } catch (RecursoNaoEncontradoException esperado) {
            assertThat(esperado.getMessage()).contains("99");
        }
    }

    @Test
    void atualizarComEmailDeOutroClienteLancaConflito() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteComId(1L)));
        when(clienteRepository.existsByEmailIgnoreCaseAndIdNot("outro@example.com", 1L)).thenReturn(true);

        try {
            service().atualizar(1L, request("outro@example.com"));
            fail("Deveria lançar ConflitoException");
        } catch (ConflitoException esperado) {
            assertThat(esperado.getMessage()).contains("e-mail");
        }
    }

    @Test
    void atualizarClienteExistenteAlteraCampos() {
        Cliente existente = clienteComId(1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByEmailIgnoreCaseAndIdNot("novo@example.com", 1L)).thenReturn(false);
        when(clienteRepository.saveAndFlush(existente)).thenReturn(existente);

        ClienteResponse resposta = service().atualizar(1L,
                new ClienteRequest("Novo Nome", "novo@example.com", "27911112222", false));

        assertThat(resposta.nome()).isEqualTo("Novo Nome");
        assertThat(resposta.email()).isEqualTo("novo@example.com");
        assertThat(resposta.ativo()).isFalse();
    }

    @Test
    void removerSemRestricaoExcluiCliente() {
        Cliente existente = clienteComId(1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));

        service(new RestricaoFixa(false)).remover(1L);

        verify(clienteRepository).delete(existente);
    }

    @Test
    void removerClienteComHistoricoLancaConflito() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteComId(1L)));

        try {
            service(new RestricaoFixa(true)).remover(1L);
            fail("Deveria lançar ConflitoException");
        } catch (ConflitoException esperado) {
            assertThat(esperado.getMessage()).contains("pedidos");
        }
        verify(clienteRepository, never()).delete(any());
    }
}
