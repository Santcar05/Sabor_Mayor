package com.sabormayor.common.error;

import org.springframework.http.HttpStatus;

/** Base class for business exceptions translated to RFC 7807 ProblemDetail responses. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
