package br.com.burgerexpress.shared.exception;

import org.springframework.http.HttpStatus;

/** Dados inválidos ou violação de regra de entrada (HTTP 400). */
public class DadosInvalidosException extends ApiException {

    public DadosInvalidosException(String detail) {
        super(HttpStatus.BAD_REQUEST, "Dados inválidos", detail);
    }
}