package com.dogsocial.dog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DogRepository extends JpaRepository<Dog, Long> {
  Page<Dog> findByOwnerId(Long ownerId, Pageable pageable);
}

