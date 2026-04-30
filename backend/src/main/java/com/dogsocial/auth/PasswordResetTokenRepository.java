package com.dogsocial.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  Optional<PasswordResetToken> findByTokenHash(String tokenHash);

  boolean existsByUser_EmailAndCreatedAtAfter(String email, Instant createdAfter);

  @Modifying
  @Query("update PasswordResetToken t set t.usedAt = :usedAt where t.user.id = :userId and t.usedAt is null")
  void markUnusedTokensForUserAsUsed(@Param("userId") Long userId, @Param("usedAt") Instant usedAt);

  @Modifying
  void deleteByExpiresAtBefore(Instant cutoff);
}
