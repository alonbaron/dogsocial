package com.dogsocial.follow;

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
    name = "follows",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_follows_follower_followed", columnNames = {"follower_id", "followed_id"})
    },
    indexes = {
        @Index(name = "idx_follows_follower_id", columnList = "follower_id"),
        @Index(name = "idx_follows_followed_id", columnList = "followed_id")
    }
)
public class Follow {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "follower_id", nullable = false, foreignKey = @ForeignKey(name = "fk_follows_follower"))
  private User follower;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "followed_id", nullable = false, foreignKey = @ForeignKey(name = "fk_follows_followed"))
  private User followed;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}

