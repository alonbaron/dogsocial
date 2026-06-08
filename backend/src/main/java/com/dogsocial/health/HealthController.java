package com.dogsocial.health;

import com.dogsocial.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public health endpoint to verify database connectivity.
 * No auth required — use for step-by-step debugging.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

  private final UserRepository userRepository;

  public HealthController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> health() {
    return ResponseEntity.ok(Map.of("status", "up"));
  }

  @GetMapping("/db")
  public ResponseEntity<Map<String, Object>> db() {
    try {
      long count = userRepository.count();
      return ResponseEntity.ok(Map.of(
          "status", "up",
          "database", "ok",
          "userCount", count
      ));
    } catch (Exception e) {
      return ResponseEntity
          .status(503)
          .body(Map.of(
              "status", "down",
              "database", "error",
              "message", e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : "(no message)")
          ));
    }
  }
}
