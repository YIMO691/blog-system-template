package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "categories", indexes = @Index(name = "idx_categories_name", columnList = "name", unique = true))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80, unique = true)
  private String name;

  @Column(length = 255)
  private String description;

  @Column(nullable = false)
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
