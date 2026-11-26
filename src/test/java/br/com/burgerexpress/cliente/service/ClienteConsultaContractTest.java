package br.com.burgerexpress.cliente.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import br.com.burgerexpress.cliente.dto.ClienteResumo;
import br.com.burgerexpress.cliente.entity.Cliente;
import br.com.burgerexpress.shared.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Teste de contrato (LSP): toda implementação de ClienteConsulta deve se comportar igual
 * do ponto de vista do cliente da abstração. Subclasses fornecem a implementação a verificar.
 */
public abstract class ClienteConsultaContractTest {

    protected static final Long ID_EXISTENTE = 1L;
    protected static final Long ID_INEXISTENTE = 99L;

    protected static Cliente clienteExistente() {
        Cliente c = new Cliente("Maria", "maria@example.com", "27999998888");
        ReflectionTestUtils.setField(c, "id", ID_EXISTENTE);
        return c;
    }

    /** Retorna uma implementação que conhece somente o cliente com ID_EXISTENTE. */
    protected abstract ClienteConsulta implementacaoComClienteExistente(Cliente cliente);

    @Test
    void retornaResumoDoClienteExistente() {
        ClienteConsulta consulta = implementacaoComClienteExistente(clienteExistente());

        ClienteResumo resumo = consulta.buscarPorId(ID_EXISTENTE);

        assertThat(resumo).isNotNull();
        assertThat(resumo.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resumo.nome()).isEqualTo("Maria");
        assertThat(resumo.email()).isEqualTo("maria@example.com");
        assertThat(resumo.ativo()).isTrue();
    }

    @Test
    void lancaNaoEncontradoParaClienteInexistenteSemRetornarNull() {
        ClienteConsulta consulta = implementacaoComClienteExistente(clienteExistente());

        try {
            consulta.buscarPorId(ID_INEXISTENTE);
            fail("Deveria lançar RecursoNaoEncontradoException");
        } catch (RecursoNaoEncontradoException esperado) {
            assertThat(esperado.getMessage()).isNotBlank();
        }
    }
}
