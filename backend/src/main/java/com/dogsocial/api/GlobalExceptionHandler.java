package com.dogsocial.api;

import com.dogsocial.exception.BadRequestException;
import com.dogsocial.exception.ForbiddenException;
import com.dogsocial.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
        .map(this::toFieldError)
        .toList();
    return build(HttpStatus.BAD_REQUEST, "Validation failed", req, fieldErrors);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
    return build(HttpStatus.FORBIDDEN, ex.getMessage(), req, null);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
    return build(HttpStatus.FORBIDDEN, "Forbidden", req, null);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
    return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), req, null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
    log.error("Unexpected error at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
    String detail = ex.getClass().getSimpleName() + ": " + (ex.getMessage() != null ? ex.getMessage() : "(no message)");
    ApiError body = ApiError.builder()
        .timestamp(Instant.now().toString())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .message("Unexpected error")
        .detail(detail)
        .path(req.getRequestURI())
        .fieldErrors(null)
        .build();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private ResponseEntity<ApiError> build(
      HttpStatus status,
      String message,
      HttpServletRequest req,
      List<ApiError.FieldError> fieldErrors
  ) {
    ApiError body = ApiError.builder()
        .timestamp(Instant.now().toString())
        .status(status.value())
        .error(status.getReasonPhrase())
        .message(message)
        .detail(null)
        .path(req.getRequestURI())
        .fieldErrors(fieldErrors)
        .build();
    return ResponseEntity.status(status).body(body);
  }

  private ApiError.FieldError toFieldError(FieldError fe) {
    return ApiError.FieldError.builder()
        .field(fe.getField())
        .message(fe.getDefaultMessage())
        .build();
  }
}

