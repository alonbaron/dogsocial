package com.dogsocial.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
  private Jwt jwt = new Jwt();
  private Cors cors = new Cors();
  private Upload upload = new Upload();
  private PasswordReset passwordReset = new PasswordReset();
  private Emailjs emailjs = new Emailjs();

  @Data
  public static class Jwt {
    private String secret;
    private long expirationSeconds;
  }

  @Data
  public static class Cors {
    private String allowedOrigins;
  }

  @Data
  public static class Upload {
    private String dir = "uploads";
  }

  @Data
  public static class PasswordReset {
    private String frontendResetUrl = "http://localhost:5173/reset-password";
    private long tokenTtlMinutes = 30;
    private long emailCooldownMinutes = 5;
    private int ipHourlyLimit = 10;
    private String fromEmail = "no-reply@pawpals.local";
  }

  @Data
  public static class Emailjs {
    private String serviceId;
    private String templateId;
    private String publicKey;
  }
}

