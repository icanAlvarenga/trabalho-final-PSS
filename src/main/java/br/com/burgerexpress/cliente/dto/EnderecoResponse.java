package br.com.burgerexpress.cliente.dto;

/** Saída de endereço. */
public record EnderecoResponse(Long id, Long clienteId, String cep, String rua, String numero,
                               String bairro, String cidade, String complemento, boolean principal) {
}
