package br.com.burgerexpress.pedido.dto;

import br.com.burgerexpress.pedido.entity.StatusPedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long clienteId,
        Long enderecoId,
        Instant dataCriacao,
        StatusPedido status,
        String observacoes,
        BigDecimal total,
        List<ItemPedidoResponse> itens) {
}
