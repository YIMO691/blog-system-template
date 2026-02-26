package com.example.blog.entity;

import com.example.blog.common.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users", indexes = @Index(name = "idx_users_username", columnList = "username", unique = true))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50, unique = true)
  private String username;

  @Column(length = 50, nullable = true)
  private String nickname;

  @Column(length = 50, nullable = true)
  private String displayName;

  @Column(length = 50, unique = true)
  private String email;

  @Column(length = 20, unique = true)
  private String phone;

  @Column(nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(nullable = false)
  private boolean muted = false;
 
  @Column(nullable = false)
  private boolean emailVerified = false;

  @PrePersist
  public void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
    if (role == null) role = Role.ROLE_USER;
  }
}
