package com.dogsocial.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);

  Page<User> findByEmailContainingIgnoreCase(String q, Pageable pageable);
  Page<User> findByIdIn(Collection<Long> ids, Pageable pageable);
}

