package br.com.burgerexpress.catalogo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Entrada do endpoint PATCH /api/v1/produtos/{id}/estoque. */
public record AtualizarEstoqueRequest(
        @NotNull(message = "é obrigatório")
        @Min(value = 0, message = "não pode ser negativo")
        Integer estoque) {
}