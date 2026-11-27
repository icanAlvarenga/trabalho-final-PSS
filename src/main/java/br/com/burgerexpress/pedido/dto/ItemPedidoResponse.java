package br.com.burgerexpress.pedido.dto;

import java.math.BigDecimal;

public record ItemPedidoResponse(
        Long id,
        Long produtoId,
        int quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal) {
}
