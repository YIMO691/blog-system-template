package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "comments", indexes = @Index(name = "idx_comments_article", columnList = "article_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id")
  private Article article;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user; // nullable -> anonymous allowed if you want

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Comment parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
  private java.util.List<Comment> replies = new java.util.ArrayList<>();

  @Column(nullable = false, length = 80)
  private String displayName;

  @Column(nullable = false, length = 500)
  private String content;

  @Column(nullable = false)
  private boolean approved;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private int likes = 0;

  @PrePersist
  public void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
