package com.mercury.mercury.Common;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> buildErrorResponseBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        body.put("status", status.value());
        body.put("message", message);
        return body;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFound(NoResourceFoundException ex) throws NoResourceFoundException {
        // Essential bypass: allows Spring Boot to serve internal static assets (Swagger UI)
        throw ex;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        Map<String, Object> responseBody = buildErrorResponseBody(HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        FieldError firstError = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);

        String message = (firstError != null) ? firstError.getDefaultMessage() : "Validation constraint violation failed.";

        Map<String, Object> responseBody = buildErrorResponseBody(HttpStatus.BAD_REQUEST, message);
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> responseBody = buildErrorResponseBody(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> responseBody = buildErrorResponseBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Debug Info -> " + ex.getClass().getSimpleName() + " : " + ex.getMessage()
        );
        return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockingFailure(
            org.springframework.orm.ObjectOptimisticLockingFailureException ex) {

        Map<String, Object> responseBody = buildErrorResponseBody(
                HttpStatus.CONFLICT, // Returns 409 Conflict status code
                "Trade has already been modified. Please refresh before editing."
        );
        return new ResponseEntity<>(responseBody, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(SettlementException.class)
    public ResponseEntity<Map<String, Object>> handleSettlementException(SettlementException ex) {
        Map<String, Object> responseBody = buildErrorResponseBody(ex.getHttpStatus(), ex.getMessage());
        return new ResponseEntity<>(responseBody, ex.getHttpStatus());
    }
}
