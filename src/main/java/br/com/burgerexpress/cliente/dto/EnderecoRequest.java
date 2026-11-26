package br.com.burgerexpress.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Entrada de cadastro de endereço. {@code complemento} e {@code principal} são opcionais. */
public record EnderecoRequest(
        @NotBlank(message = "é obrigatório")
        @Pattern(regexp = "^\\d{8}$", message = "deve conter 8 dígitos, somente números")
        String cep,

        @NotBlank(message = "é obrigatório")
        @Size(max = 150, message = "deve ter no máximo 150 caracteres")
        String rua,

        @NotBlank(message = "é obrigatório")
        @Size(max = 20, message = "deve ter no máximo 20 caracteres")
        String numero,

        @NotBlank(message = "é obrigatório")
        @Size(max = 100, message = "deve ter no máximo 100 caracteres")
        String bairro,

        @NotBlank(message = "é obrigatório")
        @Size(max = 100, message = "deve ter no máximo 100 caracteres")
        String cidade,

        @Size(max = 100, message = "deve ter no máximo 100 caracteres")
        String complemento,

        Boolean principal) {
}
