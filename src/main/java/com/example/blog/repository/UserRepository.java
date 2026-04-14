package com.example.blog.repository;

import com.example.blog.common.Role;
import com.example.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  Optional<User> findByPhone(String phone);
  List<User> findByRole(Role role);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
  boolean existsByPhone(String phone);
}
