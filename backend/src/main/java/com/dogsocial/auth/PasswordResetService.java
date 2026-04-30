package com.dogsocial.auth;

import com.dogsocial.config.AppProperties;
import com.dogsocial.exception.BadRequestException;
import com.dogsocial.user.User;
import com.dogsocial.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {
  private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
  private static final SecureRandom RANDOM = new SecureRandom();

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final AppProperties properties;
  private final JavaMailSender mailSender;
  private final Map<String, ArrayDeque<Instant>> requestsByIp = new ConcurrentHashMap<>();

  public PasswordResetService(
      UserRepository userRepository,
      PasswordResetTokenRepository tokenRepository,
      PasswordEncoder passwordEncoder,
      AppProperties properties,
      ObjectProvider<JavaMailSender> mailSenderProvider
  ) {
    this.userRepository = userRepository;
    this.tokenRepository = tokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.properties = properties;
    this.mailSender = mailSenderProvider.getIfAvailable();
  }

  @Transactional(readOnly = false)
  public void requestReset(String email, String ipAddress) {
    String normalizedEmail = normalizeEmail(email);
    enforceIpLimit(ipAddress);

    Optional<User> maybeUser = userRepository.findByEmail(normalizedEmail);
    if (maybeUser.isEmpty()) {
      return;
    }

    AppProperties.PasswordReset resetProps = properties.getPasswordReset();
    Instant now = Instant.now();
    Instant cooldownCutoff = now.minus(Duration.ofMinutes(resetProps.getEmailCooldownMinutes()));
    if (tokenRepository.existsByUser_EmailAndCreatedAtAfter(normalizedEmail, cooldownCutoff)) {
      return;
    }

    User user = maybeUser.get();
    tokenRepository.markUnusedTokensForUserAsUsed(user.getId(), now);
    tokenRepository.deleteByExpiresAtBefore(now.minus(Duration.ofDays(1)));

    String rawToken = generateToken();
    PasswordResetToken resetToken = PasswordResetToken.builder()
        .user(user)
        .tokenHash(hash(rawToken))
        .expiresAt(now.plus(Duration.ofMinutes(resetProps.getTokenTtlMinutes())))
        .requestIp(ipAddress)
        .build();
    tokenRepository.save(resetToken);

    String resetLink = UriComponentsBuilder
        .fromUriString(resetProps.getFrontendResetUrl())
        .queryParam("token", rawToken)
        .build()
        .toUriString();
    sendResetEmail(user.getEmail(), resetLink);
  }

  @Transactional(readOnly = false)
  public void resetPassword(String token, String newPassword) {
    if (token == null || token.isBlank()) {
      throw new BadRequestException("Reset token is required");
    }
    if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 72) {
      throw new BadRequestException("Password must be 8-72 characters");
    }

    PasswordResetToken resetToken = tokenRepository.findByTokenHash(hash(token))
        .orElseThrow(() -> new BadRequestException("Invalid or expired reset link"));

    Instant now = Instant.now();
    if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(now)) {
      throw new BadRequestException("Invalid or expired reset link");
    }

    User user = resetToken.getUser();
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    resetToken.setUsedAt(now);
    userRepository.save(user);
    tokenRepository.save(resetToken);
    tokenRepository.markUnusedTokensForUserAsUsed(user.getId(), now);
  }

  private void enforceIpLimit(String ipAddress) {
    String key = ipAddress == null || ipAddress.isBlank() ? "unknown" : ipAddress;
    Instant now = Instant.now();
    Instant cutoff = now.minus(Duration.ofHours(1));
    ArrayDeque<Instant> attempts = requestsByIp.computeIfAbsent(key, ignored -> new ArrayDeque<>());
    synchronized (attempts) {
      while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
        attempts.removeFirst();
      }
      if (attempts.size() >= properties.getPasswordReset().getIpHourlyLimit()) {
        throw new BadRequestException("Too many password reset requests. Please try again later.");
      }
      attempts.addLast(now);
    }
  }

  private void sendResetEmail(String to, String resetLink) {
    if (mailSender == null) {
      log.warn("SMTP is not configured. Password reset link for {}: {}", to, resetLink);
      return;
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(properties.getPasswordReset().getFromEmail());
    message.setTo(to);
    message.setSubject("Reset your PawPals password");
    message.setText("""
        We received a request to reset your PawPals password.

        Open this link to set a new password:
        %s

        This link expires soon. If you did not request this, you can ignore this email.
        """.formatted(resetLink));
    mailSender.send(message);
  }

  private static String generateToken() {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static String hash(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to hash password reset token", ex);
    }
  }

  private static String normalizeEmail(String email) {
    return email == null ? "" : email.trim().toLowerCase();
  }
}
