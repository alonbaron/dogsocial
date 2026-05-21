package com.dogsocial.auth;

import com.dogsocial.config.AppProperties;
import com.dogsocial.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {

  @Test
  void requestResetPrunesExpiredIpBuckets() throws Exception {
    UserRepository userRepository = mock(UserRepository.class);
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    PasswordResetService service = new PasswordResetService(
        userRepository,
        mock(PasswordResetTokenRepository.class),
        mock(PasswordEncoder.class),
        new AppProperties(),
        mailSenderProvider()
    );
    Map<String, ArrayDeque<Instant>> requestsByIp = requestsByIp(service);
    requestsByIp.put("198.51.100.10", new ArrayDeque<>(List.of(Instant.now().minus(Duration.ofHours(2)))));
    requestsByIp.put("198.51.100.11", new ArrayDeque<>(List.of(Instant.now().minus(Duration.ofHours(3)))));

    service.requestReset("missing@example.com", "203.0.113.5");

    assertThat(requestsByIp)
        .doesNotContainKeys("198.51.100.10", "198.51.100.11")
        .containsKey("203.0.113.5");
  }

  private static ObjectProvider<JavaMailSender> mailSenderProvider() {
    @SuppressWarnings("unchecked")
    ObjectProvider<JavaMailSender> mailSenderProvider = mock(ObjectProvider.class);
    when(mailSenderProvider.getIfAvailable()).thenReturn(null);
    return mailSenderProvider;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, ArrayDeque<Instant>> requestsByIp(PasswordResetService service) throws Exception {
    Field field = PasswordResetService.class.getDeclaredField("requestsByIp");
    field.setAccessible(true);
    return (Map<String, ArrayDeque<Instant>>) field.get(service);
  }
}
