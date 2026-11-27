package br.com.burgerexpress.pedido.service;

import br.com.burgerexpress.cliente.service.RestricaoExclusaoCliente;
import br.com.burgerexpress.pedido.repository.PedidoRepository;
import org.springframework.stereotype.Component;

@Component
class HistoricoPedidoCliente implements RestricaoExclusaoCliente {

    private final PedidoRepository pedidoRepository;

    HistoricoPedidoCliente(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public boolean impedeExclusao(Long clienteId) {
        return pedidoRepository.existsByClienteId(clienteId);
    }
}
