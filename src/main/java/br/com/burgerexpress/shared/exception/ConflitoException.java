package br.com.burgerexpress.shared.exception;

import org.springframework.http.HttpStatus;

/** Conflito de estado ou de regra de negócio (HTTP 409). */
public class ConflitoException extends ApiException {

    public ConflitoException(String detail) {
        super(HttpStatus.CONFLICT, "Conflito de estado", detail);
    }
}
