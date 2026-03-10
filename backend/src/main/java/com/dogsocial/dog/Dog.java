package com.dogsocial.dog;

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
    name = "dogs",
    indexes = {
        @Index(name = "idx_dogs_owner_id", columnList = "owner_id")
    }
)
public class Dog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dogs_owner"))
  private User owner;

  @Column(nullable = false, length = 80)
  private String name;

  @Column(length = 80)
  private String breed;

  @Column(length = 300)
  private String bio;

  @Column(name = "photo_path", length = 500)
  private String photoPath;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}

