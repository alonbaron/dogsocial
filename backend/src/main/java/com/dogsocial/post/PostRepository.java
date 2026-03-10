package com.dogsocial.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface PostRepository extends JpaRepository<Post, Long> {
  @EntityGraph(attributePaths = {"author", "dog"})
  Page<Post> findByDogId(Long dogId, Pageable pageable);

  @EntityGraph(attributePaths = {"author", "dog"})
  Page<Post> findByAuthorId(Long authorId, Pageable pageable);

  @Query("""
      select p from Post p
      where p.author.id = :me
         or p.author.id in :followedIds
      """)
  @EntityGraph(attributePaths = {"author", "dog"})
  Page<Post> feed(@Param("me") Long me, @Param("followedIds") Collection<Long> followedIds, Pageable pageable);

  @EntityGraph(attributePaths = {"author", "dog"})
  @Query(
      value = "select p from Post p",
      countQuery = "select count(p) from Post p"
  )
  Page<Post> findAllPostsOrderByCreatedAtDesc(Pageable pageable);
}

