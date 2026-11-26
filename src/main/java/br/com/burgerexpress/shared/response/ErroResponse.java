package br.com.burgerexpress.shared.response;

/** Corpo padronizado de erro de toda a API. */
public record ErroResponse(int status, String title, String detail, String path) {
}
