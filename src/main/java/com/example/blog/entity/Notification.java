package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_recipient", columnList = "recipient_id"),
    @Index(name = "idx_notifications_created_at", columnList = "createdAt")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  private com.example.blog.common.NotificationType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipient_id")
  private User recipient;

  @Column(nullable = false, length = 300)
  private String message;

  @Column(length = 200)
  private String link;

  @Column(name = "is_read", nullable = false)
  private boolean read;

  @Column(nullable = false)
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
