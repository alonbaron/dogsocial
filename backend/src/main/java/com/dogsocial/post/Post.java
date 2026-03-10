package com.dogsocial.post;

import com.dogsocial.dog.Dog;
import com.dogsocial.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "posts",
    indexes = {
        @Index(name = "idx_posts_author_id_created_at", columnList = "author_id,created_at"),
        @Index(name = "idx_posts_dog_id_created_at", columnList = "dog_id,created_at")
    }
)
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_posts_author"))
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dog_id", foreignKey = @ForeignKey(name = "fk_posts_dog"))
  private Dog dog;

  @Column(nullable = false, length = 300)
  private String caption;

  @Column(name = "image_path", length = 500)
  private String imagePath;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
