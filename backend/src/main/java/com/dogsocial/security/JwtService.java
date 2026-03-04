package com.dogsocial.security;

import com.dogsocial.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
  private final AppProperties props;
  private SecretKey key;

  public JwtService(AppProperties props) {
    this.props = props;
  }

  @PostConstruct
  void init() {
    byte[] secretBytes = props.getJwt().getSecret() == null
        ? new byte[0]
        : props.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
    byte[] keyBytes = sha256(secretBytes);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateToken(Long userId, String email, UserRole role) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.getJwt().getExpirationSeconds());
    return Jwts.builder()
        .setSubject(email)
        .claim("uid", userId)
        .claim("role", role.name())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(exp))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Claims parseAndValidate(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private static byte[] sha256(byte[] input) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(input);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to init JWT key", e);
    }
  }
}

