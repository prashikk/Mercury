package com.mercury.mercury.Common;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
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
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<Map<String, Object>> handleSettlementException(BusinessValidationException ex) {
        Map<String, Object> responseBody = buildErrorResponseBody(ex.getHttpStatus(), ex.getMessage());
        return new ResponseEntity<>(responseBody, ex.getHttpStatus());
    }

    @ExceptionHandler(com.mercury.mercury.Portfolio.exception.InsufficientHoldingsException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientHoldings(com.mercury.mercury.Portfolio.exception.InsufficientHoldingsException ex) {
        Map<String, Object> responseBody = buildErrorResponseBody(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access Denied: {}", ex.getMessage()); // Task 6: Log Access Denied

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("message", "Access Denied");

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

}
