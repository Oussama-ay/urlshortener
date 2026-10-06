package com.nova.urlshortener.exception;

import com.nova.urlshortener.ratelimit.RateLimitExceededException;
import com.nova.urlshortener.url.InvalidExpirationException;
import com.nova.urlshortener.url.InvalidUrlException;
import com.nova.urlshortener.url.UrlExpiredException;
import com.nova.urlshortener.url.UrlNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.nova.urlshortener.user.EmailAlreadyExistsException;
import org.springframework.security.core.AuthenticationException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationFailure(
            AuthenticationException exception) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExists(EmailAlreadyExistsException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(UrlNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ApiError> handleExpired(UrlExpiredException exception) {
        return error(HttpStatus.GONE, exception.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimit(RateLimitExceededException exception) {
        return error(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage());
    }

    @ExceptionHandler({InvalidUrlException.class, InvalidExpirationException.class})
    public ResponseEntity<ApiError> handleInvalidRequest(RuntimeException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .distinct()
                .sorted()
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedBody(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST, "Malformed request body or invalid field format");
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), message, OffsetDateTime.now()));
    }
}
