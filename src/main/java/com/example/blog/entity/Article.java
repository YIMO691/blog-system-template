package com.example.blog.entity;

import com.example.blog.common.ArticleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "articles", indexes = {
    @Index(name = "idx_articles_status", columnList = "status"),
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
  @Column(nullable = false, columnDefinition = "LONGTEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  @Builder.Default
  private ArticleStatus status = ArticleStatus.DRAFT;

  @Column(nullable = false)
  @Builder.Default
  private boolean published = false;

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
  @Builder.Default
  private long views = 0;

  @Column(nullable = false)
  @Builder.Default
  private int likes = 0;

  @Transient
  private String summary;

  @Transient
  @Builder.Default
  private Set<String> searchMatchSources = new LinkedHashSet<>();

  @Transient
  private String highlightedTitle;

  @Transient
  private String highlightedSummary;

  @PrePersist
  public void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
    if (status == null) status = ArticleStatus.DRAFT;
    syncPublishedFromStatus();
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = Instant.now();
    if (status == null) {
      status = published ? ArticleStatus.PUBLISHED : ArticleStatus.DRAFT;
    }
    syncPublishedFromStatus();
  }

  public void setStatus(ArticleStatus status) {
    this.status = status == null ? ArticleStatus.DRAFT : status;
    syncPublishedFromStatus();
  }

  private void syncPublishedFromStatus() {
    this.published = this.status == ArticleStatus.PUBLISHED;
  }
}
