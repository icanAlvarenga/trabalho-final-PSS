package br.com.burgerexpress.cliente.service;

import br.com.burgerexpress.cliente.dto.ClienteResumo;
import br.com.burgerexpress.cliente.entity.Cliente;
import br.com.burgerexpress.shared.exception.RecursoNaoEncontradoException;

/**
 * Verifica o mesmo contrato numa implementação alternativa (em memória), provando que ela
 * substitui o ClienteService sem alterar o comportamento esperado pelos consumidores (LSP).
 */
class ClienteConsultaEmMemoriaContractTest extends ClienteConsultaContractTest {

    /** Implementação alternativa de ClienteConsulta que guarda um único cliente em memória. */
    private static class ClienteConsultaEmMemoria implements ClienteConsulta {

        private final Cliente cliente;

        ClienteConsultaEmMemoria(Cliente cliente) {
            this.cliente = cliente;
        }

        @Override
        public ClienteResumo buscarPorId(Long clienteId) {
            if (ID_EXISTENTE.equals(clienteId)) {
                return new ClienteResumo(cliente.getId(), cliente.getNome(), cliente.getEmail(), cliente.isAtivo());
            }
            throw new RecursoNaoEncontradoException("Cliente com id " + clienteId + " não encontrado.");
        }
    }

    @Override
    protected ClienteConsulta implementacaoComClienteExistente(Cliente cliente) {
        return new ClienteConsultaEmMemoria(cliente);
    }
}
