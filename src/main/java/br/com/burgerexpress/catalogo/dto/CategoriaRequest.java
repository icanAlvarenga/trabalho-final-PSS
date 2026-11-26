package br.com.burgerexpress.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 80, message = "deve ter no máximo 80 caracteres")
        String nome,

        @Size(max = 255, message = "deve ter no máximo 255 caracteres")
        String descricao,

        Boolean ativa) {
}