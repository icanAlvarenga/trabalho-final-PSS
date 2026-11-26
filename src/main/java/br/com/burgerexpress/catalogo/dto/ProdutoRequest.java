package br.com.burgerexpress.catalogo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Entrada de criação (POST) e atualização (PUT) de produto. {@code disponivel} é opcional. */
public record ProdutoRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 120, message = "deve ter no máximo 120 caracteres")
        String nome,

        @Size(max = 500, message = "deve ter no máximo 500 caracteres")
        String descricao,

        @NotNull(message = "é obrigatório")
        @DecimalMin(value = "0.01", message = "deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "deve ter no máximo 8 dígitos inteiros e 2 decimais")
        BigDecimal preco,

        @NotNull(message = "é obrigatório")
        @Min(value = 0, message = "não pode ser negativo")
        Integer estoque,

        Boolean disponivel,

        @NotNull(message = "é obrigatório")
        Long categoriaId) {
}