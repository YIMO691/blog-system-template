package com.example.blog.config;

import com.example.blog.common.AdminPermission;
import com.example.blog.common.Role;
import com.example.blog.entity.Category;
import com.example.blog.entity.User;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    userRepository.findByUsername("admin")
        .ifPresentOrElse(admin -> {
          boolean dirty = false;
          if (admin.getRole() != Role.ROLE_ADMIN) {
            admin.setRole(Role.ROLE_ADMIN);
            dirty = true;
          }
          if (!admin.isSuperAdmin()) {
            admin.setSuperAdmin(true);
            dirty = true;
          }
          if (!admin.getAdminPermissions().containsAll(AdminPermission.superAdminPermissions())) {
            admin.grantAdminPermissions(AdminPermission.superAdminPermissions());
            dirty = true;
          }
          if (dirty) {
            userRepository.save(admin);
          }
        }, () -> userRepository.save(User.builder()
            .username("admin")
            .nickname("最高管理员")
            .passwordHash(passwordEncoder.encode("admin123456"))
            .role(Role.ROLE_ADMIN)
            .superAdmin(true)
            .adminPermissions(AdminPermission.superAdminPermissions())
            .createdAt(Instant.now())
            .build()));

    if (!categoryRepository.existsByNameIgnoreCase("默认分类")) {
      categoryRepository.save(Category.builder().name("默认分类").description("系统初始化分类").build());
    }
  }
}
