package com.mercury.mercury.Portfolio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientHoldingsException extends RuntimeException {
    public InsufficientHoldingsException(String message) {
        super(message);
    }
}
