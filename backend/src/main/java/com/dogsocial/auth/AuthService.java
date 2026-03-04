package com.dogsocial.auth;

import com.dogsocial.auth.dto.AuthDtos;
import com.dogsocial.exception.BadRequestException;
import com.dogsocial.security.JwtService;
import com.dogsocial.security.UserPrincipal;
import com.dogsocial.security.UserRole;
import com.dogsocial.user.User;
import com.dogsocial.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtService jwtService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
    String email = normalizeEmail(req.getEmail());
    if (userRepository.existsByEmail(email)) {
      throw new BadRequestException("Email already registered");
    }

    User user = User.builder()
        .email(email)
        .passwordHash(passwordEncoder.encode(req.getPassword()))
        .role(UserRole.USER)
        .build();
    user = userRepository.save(user);

    String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
    return AuthDtos.AuthResponse.builder()
        .token(token)
        .me(toMe(user))
        .build();
  }

  public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
    String email = normalizeEmail(req.getEmail());
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(email, req.getPassword())
    );
    UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

    User user = userRepository.findById(principal.getId())
        .orElseThrow(() -> new BadRequestException("User not found"));

    String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
    return AuthDtos.AuthResponse.builder()
        .token(token)
        .me(toMe(user))
        .build();
  }

  public AuthDtos.UserMe me(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BadRequestException("User not found"));
    return toMe(user);
  }

  private AuthDtos.UserMe toMe(User user) {
    return AuthDtos.UserMe.builder()
        .id(user.getId())
        .email(user.getEmail())
        .role(user.getRole().name())
        .createdAt(user.getCreatedAt() == null ? null : user.getCreatedAt().toString())
        .build();
  }

  private static String normalizeEmail(String email) {
    return email == null ? null : email.trim().toLowerCase();
  }
}

