package com.dogsocial.reaction;

import com.dogsocial.post.Post;
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
    name = "post_reactions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_reactions_post_user", columnNames = {"post_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_post_reactions_post_id_type", columnList = "post_id,type"),
        @Index(name = "idx_post_reactions_user_id", columnList = "user_id")
    }
)
public class PostReaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false, foreignKey = @ForeignKey(name = "fk_post_reactions_post"))
  private Post post;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_post_reactions_user"))
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReactionType type;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}

