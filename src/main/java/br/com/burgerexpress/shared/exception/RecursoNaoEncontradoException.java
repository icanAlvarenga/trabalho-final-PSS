package br.com.burgerexpress.shared.exception;

import org.springframework.http.HttpStatus;

/** Recurso inexistente (HTTP 404). */
public class RecursoNaoEncontradoException extends ApiException {

    public RecursoNaoEncontradoException(String detail) {
        super(HttpStatus.NOT_FOUND, "Recurso não encontrado", detail);
    }
}
