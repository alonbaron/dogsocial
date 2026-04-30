package com.dogsocial.reaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentReactionRepository extends JpaRepository<CommentReaction, Long> {
  Optional<CommentReaction> findByCommentIdAndUserId(Long commentId, Long userId);
  List<CommentReaction> findByCommentIdInAndUserId(Collection<Long> commentIds, Long userId);

  @Query("select r.comment.id, r.type from CommentReaction r where r.comment.id in :commentIds and r.user.id = :userId")
  List<Object[]> findCommentIdAndTypeByCommentIdInAndUserId(@Param("commentIds") Collection<Long> commentIds, @Param("userId") Long userId);

  void deleteByCommentIdAndUserId(Long commentId, Long userId);

  @Modifying
  void deleteByCommentId(Long commentId);

  @Modifying
  @Query("delete from CommentReaction r where r.comment.post.id = :postId")
  void deleteByPostId(@Param("postId") Long postId);

  @Query("""
      select r.comment.id, r.type, count(r)
      from CommentReaction r
      where r.comment.id in :commentIds
      group by r.comment.id, r.type
      """)
  List<Object[]> countByCommentIds(@Param("commentIds") Collection<Long> commentIds);
}

