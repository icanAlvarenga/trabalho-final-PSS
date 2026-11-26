package br.com.burgerexpress.cliente.dto;

/** Saída de cliente. Entidades JPA nunca são expostas diretamente. */
public record ClienteResponse(Long id, String nome, String email, String telefone, boolean ativo) {
}
