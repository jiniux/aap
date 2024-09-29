package xyz.jiniux.aap.controllers;

import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.Serializable;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    public record ErrorResponse(String code, Object details) implements Serializable {}

    @ExceptionHandler(value = {DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse dataIntegrityViolationException(DataIntegrityViolationException ignoredEx) {
        return new ErrorResponse("DATA_INTEGRITY_VIOLATION", Map.of());
    }

    @ExceptionHandler(value = {OptimisticLockException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse optimisticLockException(DataIntegrityViolationException ignoredEx) {
        return new ErrorResponse("CONCURRENT_MODIFICATION_OCCURRED", Map.of());
    }
}
