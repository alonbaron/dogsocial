package com.dogsocial.api;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void dataIntegrityViolationReturnsBadRequestForDuplicateEmail() {
    HttpServletRequest request = request("/api/auth/register");
    DataIntegrityViolationException exception = new DataIntegrityViolationException(
        "Duplicate entry 'a@example.com' for key 'uk_users_email'"
    );

    ResponseEntity<ApiError> response = handler.handleDataIntegrityViolation(exception, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody())
        .extracting(ApiError::getMessage, ApiError::getPath)
        .containsExactly("Email already registered", "/api/auth/register");
  }

  @Test
  void dataIntegrityViolationReturnsBadRequestForDuplicateFollow() {
    HttpServletRequest request = request("/api/users/2/follow");
    DataIntegrityViolationException exception = new DataIntegrityViolationException(
        "Duplicate entry '1-2' for key 'uk_follows_follower_followed'"
    );

    ResponseEntity<ApiError> response = handler.handleDataIntegrityViolation(exception, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody())
        .extracting(ApiError::getMessage, ApiError::getPath)
        .containsExactly("Already following this user", "/api/users/2/follow");
  }

  private static HttpServletRequest request(String path) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn(path);
    return request;
  }
}
