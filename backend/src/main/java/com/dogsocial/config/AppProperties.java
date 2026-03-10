package com.dogsocial.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
  private Jwt jwt = new Jwt();
  private Cors cors = new Cors();
  private Upload upload = new Upload();

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
}

