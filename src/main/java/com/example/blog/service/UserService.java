package com.example.blog.service;

import com.example.blog.dto.RegisterRequest;
import com.example.blog.entity.User;

public interface UserService {
  record NicknameUpdateResult(String nickname, String message) {}

  record PasswordUpdateResult(String message) {}

  record EmailUpdateResult(String email, boolean emailVerified, String message) {}

  record PhoneUpdateResult(String phone, String message) {}

  User register(RegisterRequest request);
  User getCurrentUserOrThrow();
  void resetPasswordByEmail(String email, String code, String newPassword);
  void assertCanAccessAdmin();
  void assertCanManageUsers();
  void assertCanManageArticles();
  void assertCanModerateComments();
  void assertCanManageAdminNotifications();
  void assertCanViewStats();
  void assertCanWriteArticles();
  User toggleAdminRole(Long userId);
  User toggleMute(Long userId);
  NicknameUpdateResult updateNickname(String nickname);
  PasswordUpdateResult updatePassword(String oldPassword, String newPassword);
  EmailUpdateResult updateEmail(String newEmail, String code);
  PhoneUpdateResult updatePhone(String phone);
}
