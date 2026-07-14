package com.mercury.mercury.Common;

import org.springframework.http.HttpStatus;

public class SettlementException extends RuntimeException {
    private final HttpStatus httpStatus;

    public SettlementException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}