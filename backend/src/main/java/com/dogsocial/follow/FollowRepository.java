package com.dogsocial.follow;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
  Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);
  boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
  void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);
  long countByFollowedId(Long followedId);
  long countByFollowerId(Long followerId);

  @Query("select f.followed.id from Follow f where f.follower.id = :followerId")
  List<Long> findFollowedIds(@Param("followerId") Long followerId);

  @Query("select f.follower.id from Follow f where f.followed.id = :followedId")
  List<Long> findFollowerIds(@Param("followedId") Long followedId);

  @EntityGraph(attributePaths = {"followed"})
  Page<Follow> findByFollowerId(Long followerId, Pageable pageable);

  @EntityGraph(attributePaths = {"follower"})
  Page<Follow> findByFollowedId(Long followedId, Pageable pageable);
}

