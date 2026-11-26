package br.com.burgerexpress.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Base das exceções de domínio. Cada módulo pode criar subclasses (ex.: TransicaoInvalidaException)
 * informando o status HTTP e o título; o handler global não precisa ser alterado (OCP).
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String title;

    protected ApiException(HttpStatus status, String title, String detail) {
        super(detail);
        this.status = status;
        this.title = title;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }
}
