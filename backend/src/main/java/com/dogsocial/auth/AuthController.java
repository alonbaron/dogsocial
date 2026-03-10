package com.dogsocial.auth;

import com.dogsocial.auth.dto.AuthDtos;
import com.dogsocial.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
    return authService.register(req);
  }

  @PostMapping("/login")
  public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
    return authService.login(req);
  }

  @GetMapping("/me")
  public AuthDtos.UserMe me() {
    return authService.me(SecurityUtils.requireUserId());
  }
}

