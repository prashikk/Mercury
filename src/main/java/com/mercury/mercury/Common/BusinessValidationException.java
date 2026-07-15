package com.mercury.mercury.Common;

import org.springframework.http.HttpStatus;

public class BusinessValidationException extends RuntimeException {
    private final HttpStatus httpStatus;

    public BusinessValidationException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}