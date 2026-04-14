package com.example.blog.common;

import java.util.EnumSet;
import java.util.Set;

public enum AdminPermission {
  ARTICLE_WRITE("文章写作"),
  ARTICLE_MANAGE("文章管理"),
  COMMENT_MODERATE("评论审核"),
  NOTIFICATION_MANAGE("后台通知"),
  STATS_VIEW("系统统计"),
  USER_MANAGE("用户管理");

  private final String label;

  AdminPermission(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public String getAuthority() {
    return "ADMIN_" + name();
  }

  public static Set<AdminPermission> defaultAdminPermissions() {
    return EnumSet.of(
        ARTICLE_WRITE,
        ARTICLE_MANAGE,
        COMMENT_MODERATE,
        NOTIFICATION_MANAGE,
        STATS_VIEW
    );
  }

  public static Set<AdminPermission> superAdminPermissions() {
    return EnumSet.allOf(AdminPermission.class);
  }
}
