package br.com.burgerexpress.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Entrada de criação (POST) e atualização (PUT) de cliente. {@code ativo} é opcional. */
public record ClienteRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 120, message = "deve ter no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "é obrigatório")
        @Email(message = "deve ser um e-mail válido")
        @Size(max = 160, message = "deve ter no máximo 160 caracteres")
        String email,

        @NotBlank(message = "é obrigatório")
        @Pattern(regexp = "^\\d{10,11}$", message = "deve conter 10 ou 11 dígitos, somente números")
        String telefone,

        Boolean ativo) {
}
