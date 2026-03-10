package com.dogsocial.reaction;

import com.dogsocial.comment.Comment;
import com.dogsocial.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "comment_reactions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_comment_reactions_comment_user", columnNames = {"comment_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_comment_reactions_comment_id_type", columnList = "comment_id,type"),
        @Index(name = "idx_comment_reactions_user_id", columnList = "user_id")
    }
)
public class CommentReaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "comment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_reactions_comment"))
  private Comment comment;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_reactions_user"))
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReactionType type;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}

