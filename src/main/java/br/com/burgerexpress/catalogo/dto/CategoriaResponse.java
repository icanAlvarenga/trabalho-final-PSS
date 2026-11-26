package br.com.burgerexpress.catalogo.dto;

/** Saída de categoria. Entidades JPA nunca são expostas diretamente. */
public record CategoriaResponse(
        Long id,
        String nome,
        String descricao,
        boolean ativa) {
}