package com.dogsocial.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

public class AuthDtos {
  @Data
  public static class RegisterRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(
        regexp = "^[a-zA-Z0-9_]{3,30}$",
        message = "Username must be 3-30 characters and contain only letters, numbers, or underscores"
    )
    private String username;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;
  }

  @Data
  public static class LoginRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
  }

  @Data
  @Builder
  public static class AuthResponse {
    private String token;
    private UserMe me;
  }

  @Data
  @Builder
  public static class UserMe {
    private Long id;
    private String email;
    private String username;
    private String role;
    private String createdAt;
    private String bio;
  }
}
