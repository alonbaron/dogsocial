package com.dogsocial.auth;

import com.dogsocial.auth.dto.AuthDtos;
import com.dogsocial.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final PasswordResetService passwordResetService;

  public AuthController(AuthService authService, PasswordResetService passwordResetService) {
    this.authService = authService;
    this.passwordResetService = passwordResetService;
  }

  @PostMapping("/register")
  public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
    return authService.register(req);
  }

  @PostMapping("/login")
  public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
    return authService.login(req);
  }

  @PostMapping("/forgot-password")
  public void forgotPassword(@Valid @RequestBody AuthDtos.ForgotPasswordRequest req, jakarta.servlet.http.HttpServletRequest request) {
    passwordResetService.requestReset(req.getEmail(), clientIp(request));
  }

  @PostMapping("/reset-password")
  public void resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest req) {
    passwordResetService.resetPassword(req.getToken(), req.getPassword());
  }

  @GetMapping("/me")
  public AuthDtos.UserMe me() {
    return authService.me(SecurityUtils.requireUserId());
  }

  private static String clientIp(jakarta.servlet.http.HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}

