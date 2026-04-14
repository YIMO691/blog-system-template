package com.example.blog.entity;

import com.example.blog.common.AdminPermission;
import com.example.blog.common.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

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
  @Builder.Default
  private boolean enabled = true;

  @Column(nullable = false)
  @Builder.Default
  private boolean muted = false;
 
  @Column(nullable = false)
  @Builder.Default
  private boolean emailVerified = false;

  @Column(name = "super_admin", nullable = false)
  @Builder.Default
  private boolean superAdmin = false;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_admin_permissions", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "permission", nullable = false, length = 50)
  @Builder.Default
  private Set<AdminPermission> adminPermissions = new LinkedHashSet<>();

  @PrePersist
  public void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
    if (role == null) role = Role.ROLE_USER;
  }

  public boolean isAdmin() {
    return this.role == Role.ROLE_ADMIN;
  }

  public boolean canAccessAdmin() {
    return isAdmin() && (superAdmin || !adminPermissions.isEmpty());
  }

  public boolean canWriteArticles() {
    return hasAdminPermission(AdminPermission.ARTICLE_WRITE);
  }

  public boolean canManageArticles() {
    return hasAdminPermission(AdminPermission.ARTICLE_MANAGE);
  }

  public boolean canModerateComments() {
    return hasAdminPermission(AdminPermission.COMMENT_MODERATE);
  }

  public boolean canManageAdminNotifications() {
    return hasAdminPermission(AdminPermission.NOTIFICATION_MANAGE);
  }

  public boolean canViewStats() {
    return hasAdminPermission(AdminPermission.STATS_VIEW);
  }

  public boolean canManageUsers() {
    return hasAdminPermission(AdminPermission.USER_MANAGE);
  }

  public void grantAdminPermissions(Set<AdminPermission> permissions) {
    this.adminPermissions.clear();
    if (permissions != null) {
      this.adminPermissions.addAll(permissions);
    }
  }

  private boolean hasAdminPermission(AdminPermission permission) {
    return isAdmin() && (superAdmin || adminPermissions.contains(permission));
  }
}
