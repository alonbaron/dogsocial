package com.dogsocial.reaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
  Optional<PostReaction> findByPostIdAndUserId(Long postId, Long userId);
  List<PostReaction> findByPostIdInAndUserId(Collection<Long> postIds, Long userId);

  @Query("select r.post.id, r.type from PostReaction r where r.post.id in :postIds and r.user.id = :userId")
  List<Object[]> findPostIdAndTypeByPostIdInAndUserId(@Param("postIds") Collection<Long> postIds, @Param("userId") Long userId);

  void deleteByPostIdAndUserId(Long postId, Long userId);

  @Modifying
  void deleteByPostId(Long postId);

  @Query("""
      select r.post.id, r.type, count(r)
      from PostReaction r
      where r.post.id in :postIds
      group by r.post.id, r.type
      """)
  List<Object[]> countByPostIds(@Param("postIds") Collection<Long> postIds);
}

