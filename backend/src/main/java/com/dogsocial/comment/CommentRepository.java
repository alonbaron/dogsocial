package com.dogsocial.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  @EntityGraph(attributePaths = {"author", "post"})
  Page<Comment> findByPostId(Long postId, Pageable pageable);

  @Modifying
  void deleteByPostId(Long postId);
}

