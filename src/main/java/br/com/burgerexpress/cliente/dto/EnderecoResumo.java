package br.com.burgerexpress.cliente.dto;

/**
 * Visão enxuta de um endereço, usada no CONTRATO entre módulos (ex.: Pedidos).
 * É independente do EnderecoResponse da API.
 */
public record EnderecoResumo(Long id, Long clienteId, String cep, String rua, String numero,
                             String bairro, String cidade, String complemento, boolean principal) {
}
