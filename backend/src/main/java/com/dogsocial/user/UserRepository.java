package com.dogsocial.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);

  Optional<User> findByUsername(String username);
  boolean existsByUsername(String username);

  @Query("select u from User u where lower(u.email) like lower(concat('%', :q, '%')) or lower(u.username) like lower(concat('%', :q, '%'))")
  Page<User> searchByEmailOrUsername(@Param("q") String q, Pageable pageable);

  Page<User> findByEmailContainingIgnoreCase(String q, Pageable pageable);
  Page<User> findByIdIn(Collection<Long> ids, Pageable pageable);
}
