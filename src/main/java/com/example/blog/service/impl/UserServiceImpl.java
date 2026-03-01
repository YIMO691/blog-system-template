package com.example.blog.service.impl;

import com.example.blog.common.Role;
import com.example.blog.dto.RegisterRequest;
import com.example.blog.entity.User;
import com.example.blog.exception.BadRequestException;
import com.example.blog.exception.NotFoundException;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final com.example.blog.service.EmailCodeService emailCodeService;

  @Override
  public User register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.username())) {
      throw new BadRequestException("用户名已存在");
    }
    
    String email = request.email();
    if (userRepository.existsByEmail(email)) {
      throw new BadRequestException("邮箱已被注册");
    }
    if (request.code() == null || request.code().isBlank() || !emailCodeService.verify(email, request.code())) {
      throw new BadRequestException("邮箱验证码无效或已过期");
    }
    String phone = (request.phone() == null || request.phone().isBlank()) ? null : request.phone();
    if (phone != null) {
      if (!phone.matches("\\d{11}")) {
        throw new BadRequestException("手机号需为11位数字");
      }
      if (userRepository.existsByPhone(phone)) {
        throw new BadRequestException("手机号已被注册");
      }
    }

    User u = User.builder()
        .username(request.username())
        .nickname(request.username())
        .displayName(request.username())
        .email(email)
        .phone(phone)
        .passwordHash(passwordEncoder.encode(request.password()))
        .role(Role.ROLE_USER)
        .createdAt(Instant.now())
        .enabled(true)
        .muted(false)
        .build();
    return userRepository.save(u);
  }

  @Override
  public User getCurrentUserOrThrow() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
      throw new NotFoundException("未登录");
    }
    return userRepository.findByUsername(auth.getName())
        .orElseThrow(() -> new NotFoundException("用户不存在"));
  }

  @Override
  public void resetPasswordByEmail(String email, String code, String newPassword) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundException("邮箱未注册"));
    if (code == null || code.isBlank() || !emailCodeService.verify(email, code)) {
      throw new BadRequestException("邮箱验证码无效或已过期");
    }
    if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
      throw new BadRequestException("新密码不能与旧密码相同");
    }
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }
}
