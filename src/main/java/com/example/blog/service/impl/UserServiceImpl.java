package com.example.blog.service.impl;

import com.example.blog.common.AdminPermission;
import com.example.blog.common.NotificationType;
import com.example.blog.common.Role;
import com.example.blog.common.api.ErrorCode;
import com.example.blog.dto.RegisterRequest;
import com.example.blog.entity.ActionLog;
import com.example.blog.entity.User;
import com.example.blog.exception.BadRequestException;
import com.example.blog.exception.ForbiddenException;
import com.example.blog.exception.NotFoundException;
import com.example.blog.repository.ActionLogRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.NotificationService;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final com.example.blog.service.EmailCodeService emailCodeService;
  private final NotificationService notificationService;
  private final ActionLogRepository actionLogRepository;

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

  @Override
  public void assertCanAccessAdmin() {
    User currentUser = getCurrentUserOrThrow();
    if (!currentUser.canAccessAdmin()) {
      throw new ForbiddenException(ErrorCode.NO_PERMISSION, "无权访问后台");
    }
  }

  @Override
  public void assertCanManageUsers() {
    User currentUser = getCurrentUserOrThrow();
    if (!currentUser.canManageUsers()) {
      throw new ForbiddenException(ErrorCode.NO_PERMISSION, "无权管理用户");
    }
  }

  @Override
  public void assertCanManageArticles() {
    User currentUser = getCurrentUserOrThrow();
    if (!currentUser.canManageArticles()) {
      throw new ForbiddenException(ErrorCode.NO_PERMISSION, "无权管理文章");
    }
  }

  @Override
  public void assertCanModerateComments() {
    User currentUser = getCurrentUserOrThrow();
    if (!currentUser.canModerateComments()) {
      throw new ForbiddenException(ErrorCode.NO_PERMISSION, "无权审核评论");
    }
  }

  @Override
  public void assertCanManageAdminNotifications() {
    User currentUser = getCurrentUserOrThrow();
    if (!currentUser.canManageAdminNotifications()) {
      throw new ForbiddenException(ErrorCode.NO_PERMISSION, "无权管理后台通知");
    }
  }

  @Override
  public void assertCanViewStats() {
    User currentUser = getCurrentUserOrThrow();
    if (!currentUser.canViewStats()) {
      throw new ForbiddenException(ErrorCode.NO_PERMISSION, "无权查看统计信息");
    }
  }

  @Override
  public void assertCanWriteArticles() {
    User currentUser = getCurrentUserOrThrow();
    if (!currentUser.canWriteArticles()) {
      throw new ForbiddenException(ErrorCode.NO_PERMISSION, "仅管理员可进行文章书写");
    }
  }

  @Override
  @Transactional
  public User toggleAdminRole(Long userId) {
    assertCanManageUsers();
    User user = getManagedUser(userId, ErrorCode.CANNOT_MODIFY_SUPER_ADMIN, "不能修改超级管理员");
    if (user.getRole() == Role.ROLE_ADMIN) {
      user.setRole(Role.ROLE_USER);
      user.setSuperAdmin(false);
      user.getAdminPermissions().clear();
    } else {
      user.setRole(Role.ROLE_ADMIN);
      user.grantAdminPermissions(AdminPermission.defaultAdminPermissions());
    }
    User saved = userRepository.save(user);
    notificationService.notifyUser(
        saved.getId(),
        NotificationType.USER_ROLE_CHANGED,
        "你的用户角色已更新",
        "/profile"
    );
    return saved;
  }

  @Override
  @Transactional
  public User toggleMute(Long userId) {
    assertCanManageUsers();
    User user = getManagedUser(userId, ErrorCode.CANNOT_MUTE_SUPER_ADMIN, "不能禁言超级管理员");
    user.setMuted(!user.isMuted());
    User saved = userRepository.save(user);
    notificationService.notifyUser(
        saved.getId(),
        saved.isMuted() ? NotificationType.USER_MUTED : NotificationType.USER_UNMUTED,
        saved.isMuted() ? "你的账号已被禁言" : "你的账号已解除禁言",
        "/profile"
    );
    return saved;
  }

  @Override
  @Transactional
  public NicknameUpdateResult updateNickname(String nickname) {
    User current = getCurrentUserOrThrow();
    current.setNickname(nickname);
    userRepository.save(current);
    saveActionLog(current, "修改昵称", "昵称更新为：" + nickname);
    return new NicknameUpdateResult(nickname, "昵称修改成功");
  }

  @Override
  @Transactional
  public PasswordUpdateResult updatePassword(String oldPassword, String newPassword) {
    User current = getCurrentUserOrThrow();
    if (!passwordEncoder.matches(oldPassword, current.getPasswordHash())) {
      throw new BadRequestException("原密码错误");
    }
    if (passwordEncoder.matches(newPassword, current.getPasswordHash())) {
      throw new BadRequestException("新密码不能与旧密码相同");
    }
    current.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(current);
    saveActionLog(current, "修改密码", "用户修改了登录密码");
    return new PasswordUpdateResult("密码修改成功");
  }

  @Override
  @Transactional
  public EmailUpdateResult updateEmail(String newEmail, String code) {
    User current = getCurrentUserOrThrow();
    if (newEmail == null || newEmail.isBlank()) {
      throw new BadRequestException("邮箱不能为空");
    }
    String currentEmail = current.getEmail() == null ? "" : current.getEmail();
    if (newEmail.equalsIgnoreCase(currentEmail)) {
      throw new BadRequestException("新邮箱不能与当前邮箱相同");
    }
    if (userRepository.existsByEmail(newEmail)) {
      throw new BadRequestException("该邮箱已被绑定");
    }
    if (code == null || code.isBlank() || !emailCodeService.verify(newEmail, code)) {
      throw new BadRequestException("邮箱验证码无效或已过期");
    }
    current.setEmail(newEmail);
    current.setEmailVerified(true);
    userRepository.save(current);
    saveActionLog(current, "更新邮箱", "新邮箱：" + newEmail);
    return new EmailUpdateResult(newEmail, true, "邮箱更新成功");
  }

  @Override
  @Transactional
  public PhoneUpdateResult updatePhone(String phone) {
    User current = getCurrentUserOrThrow();
    if (phone == null || phone.isBlank()) {
      current.setPhone(null);
      userRepository.save(current);
      saveActionLog(current, "解绑手机号", "手机号已解除绑定");
      return new PhoneUpdateResult("", "手机号已解除绑定");
    }
    if (!phone.matches("\\d{11}")) {
      throw new BadRequestException("手机号需为11位数字");
    }
    String currentPhone = current.getPhone() == null ? "" : current.getPhone();
    if (phone.equals(currentPhone)) {
      throw new BadRequestException("新手机号不能与当前相同");
    }
    if (userRepository.existsByPhone(phone)) {
      throw new BadRequestException("该手机号已被绑定");
    }
    current.setPhone(phone);
    userRepository.save(current);
    saveActionLog(current, "更新手机号", "新手机号：" + phone);
    return new PhoneUpdateResult(phone, "手机号更新成功");
  }

  private User getManagedUser(Long userId, ErrorCode errorCode, String message) {
    User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("用户不存在"));
    if (user.isSuperAdmin()) {
      throw new BadRequestException(errorCode, message);
    }
    return user;
  }

  private void saveActionLog(User user, String action, String detail) {
    actionLogRepository.save(ActionLog.builder()
        .user(user)
        .time(Instant.now())
        .action(action)
        .detail(detail)
        .build());
  }
}
