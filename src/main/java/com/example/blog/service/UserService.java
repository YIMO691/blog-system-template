package com.example.blog.service;

import com.example.blog.dto.RegisterRequest;
import com.example.blog.entity.User;

public interface UserService {
  User register(RegisterRequest request);
  User getCurrentUserOrThrow();
  void resetPasswordByEmail(String email, String code, String newPassword);
}
