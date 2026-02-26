package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "article_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"article_id", "user_id"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ArticleLike {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "article_id", nullable = false)
  private Article article;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
