package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "articles", indexes = {
    @Index(name = "idx_articles_published", columnList = "published"),
    @Index(name = "idx_articles_createdAt", columnList = "createdAt")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Article {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 220, unique = true)
  private String slug;

  @Lob
  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private boolean published;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @ManyToMany
  @JoinTable(name = "article_tags",
      joinColumns = @JoinColumn(name = "article_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  @Builder.Default
  private Set<Tag> tags = new LinkedHashSet<>();

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @Column(nullable = false)
  private long views = 0;

  @Column(nullable = false)
  private int likes = 0;

  @PrePersist
  public void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = Instant.now();
  }
}
