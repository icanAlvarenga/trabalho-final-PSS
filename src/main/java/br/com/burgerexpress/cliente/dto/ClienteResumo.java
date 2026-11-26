package br.com.burgerexpress.cliente.dto;

/**
 * Visão enxuta de um cliente, usada no CONTRATO entre módulos (ex.: Pedidos).
 * É independente do ClienteResponse da API, para que mudar a resposta HTTP não quebre outros módulos.
 */
public record ClienteResumo(Long id, String nome, String email, boolean ativo) {
}
