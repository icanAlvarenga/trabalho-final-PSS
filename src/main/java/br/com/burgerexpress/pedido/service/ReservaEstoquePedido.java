package br.com.burgerexpress.pedido.service;

import java.math.BigDecimal;

/** Contrato de catálogo consumido na criação de pedidos. */
public interface ReservaEstoquePedido {

    /**
     * Valida o produto e a disponibilidade, reserva na mesma transação do pedido
     * e retorna o preço unitário vigente no momento da reserva.
     */
    BigDecimal reservarEstoque(Long produtoId, int quantidade);
}
