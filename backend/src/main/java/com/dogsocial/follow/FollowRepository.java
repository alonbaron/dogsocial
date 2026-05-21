package com.dogsocial.follow;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

  @Query("select f.followed.id from Follow f where f.follower.id = :followerId and f.followed.id in :candidates")
  Set<Long> findFollowedIdsAmong(@Param("followerId") Long followerId, @Param("candidates") Collection<Long> candidates);

  @EntityGraph(attributePaths = {"followed"})
  Page<Follow> findByFollowerId(Long followerId, Pageable pageable);

  @EntityGraph(attributePaths = {"follower"})
  Page<Follow> findByFollowedId(Long followedId, Pageable pageable);
}

