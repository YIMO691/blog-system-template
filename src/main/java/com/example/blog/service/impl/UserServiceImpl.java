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

  @Override
  public User register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.username())) {
      throw new BadRequestException("用户名已存在");
    }
    
    String contact = request.contact();
    String email = null;
    String phone = null;

    if (contact.contains("@")) {
        email = contact;
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("邮箱已被注册");
        }
    } else {
        if (contact.matches("\\d{11}")) {
            phone = contact;
            if (userRepository.existsByPhone(phone)) {
                throw new BadRequestException("手机号已被注册");
            }
        } else {
            throw new BadRequestException("请输入有效的邮箱或11位手机号码");
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
}
