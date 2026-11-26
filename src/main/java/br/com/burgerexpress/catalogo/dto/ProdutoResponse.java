package br.com.burgerexpress.catalogo.dto;

import java.math.BigDecimal;

/** Saída de produto para os endpoints REST do catálogo. */
public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        boolean disponivel,
        Long categoriaId,
        String categoriaNome) {
}