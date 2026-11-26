package br.com.burgerexpress.catalogo.dto;

import java.math.BigDecimal;

/** Contrato público de saída para outros módulos (ex.: Pedidos), sem expor a entidade JPA. */
public record ProdutoResumo(
        Long id,
        String nome,
        BigDecimal preco,
        Integer estoque,
        boolean disponivel,
        Long categoriaId) {
}