package com.example.blog.config;

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
    if (!userRepository.existsByUsername("admin")) {
      userRepository.save(User.builder()
          .username("admin")
          .nickname("最高管理员")
          .passwordHash(passwordEncoder.encode("admin123456"))
          .role(Role.ROLE_ADMIN)
          .createdAt(Instant.now())
          .build());
    }
    if (!categoryRepository.existsByNameIgnoreCase("默认分类")) {
      categoryRepository.save(Category.builder().name("默认分类").description("系统初始化分类").build());
    }
  }
}
