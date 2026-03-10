package com.dogsocial.playdate;

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
    name = "playdates",
    indexes = {
        @Index(name = "idx_playdates_requester_id_status", columnList = "requester_id,status"),
        @Index(name = "idx_playdates_recipient_id_status", columnList = "recipient_id,status"),
        @Index(name = "idx_playdates_scheduled_at", columnList = "scheduled_at")
    }
)
public class Playdate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "requester_id", nullable = false, foreignKey = @ForeignKey(name = "fk_playdates_requester"))
  private User requester;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "recipient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_playdates_recipient"))
  private User recipient;

  @Column(name = "scheduled_at", nullable = false)
  private Instant scheduledAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PlaydateStatus status;

  @Column(length = 300)
  private String note;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}

