package com.example.blog.service.impl;

import com.example.blog.common.AdminPermission;
import com.example.blog.common.Role;
import com.example.blog.common.api.ErrorCode;
import com.example.blog.entity.User;
import com.example.blog.exception.BadRequestException;
import com.example.blog.exception.ForbiddenException;
import com.example.blog.repository.ActionLogRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.EmailCodeService;
import com.example.blog.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private EmailCodeService emailCodeService;

  @Mock
  private NotificationService notificationService;

  @Mock
  private ActionLogRepository actionLogRepository;

  @InjectMocks
  private UserServiceImpl userService;

  private User manager;

  @BeforeEach
  void setUp() {
    manager = User.builder()
        .id(1L)
        .username("manager")
        .passwordHash("encoded-old")
        .role(Role.ROLE_ADMIN)
        .createdAt(Instant.now())
        .adminPermissions(Set.of(AdminPermission.USER_MANAGE))
        .build();

    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("manager", "N/A"));
    when(userRepository.findByUsername("manager")).thenReturn(Optional.of(manager));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void toggleAdminRole_shouldPromoteUserAndGrantDefaultPermissions() {
    User target = User.builder()
        .id(2L)
        .username("editor")
        .role(Role.ROLE_USER)
        .createdAt(Instant.now())
        .build();
    when(userRepository.findById(2L)).thenReturn(Optional.of(target));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    User result = userService.toggleAdminRole(2L);

    assertEquals(Role.ROLE_ADMIN, result.getRole());
    assertFalse(result.getAdminPermissions().isEmpty());
    verify(notificationService).notifyUser(eq(2L), any(), eq("你的用户角色已更新"), eq("/profile"));
  }

  @Test
  void toggleMute_shouldRejectSuperAdmin() {
    User superAdmin = User.builder()
        .id(3L)
        .username("root")
        .role(Role.ROLE_ADMIN)
        .superAdmin(true)
        .createdAt(Instant.now())
        .build();
    when(userRepository.findById(3L)).thenReturn(Optional.of(superAdmin));

    BadRequestException ex = assertThrows(BadRequestException.class, () -> userService.toggleMute(3L));

    assertEquals(ErrorCode.CANNOT_MUTE_SUPER_ADMIN, ex.getErrorCode());
    verify(notificationService, never()).notifyUser(any(), any(), any(), any());
  }

  @Test
  void updateEmail_shouldPersistAndWriteActionLog() {
    when(emailCodeService.verify("new@example.com", "123456")).thenReturn(true);
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var result = userService.updateEmail("new@example.com", "123456");

    assertEquals("new@example.com", result.email());
    assertTrue(result.emailVerified());
    assertEquals("new@example.com", manager.getEmail());
    ArgumentCaptor<com.example.blog.entity.ActionLog> actionLogCaptor = ArgumentCaptor.forClass(com.example.blog.entity.ActionLog.class);
    verify(actionLogRepository).save(actionLogCaptor.capture());
    assertTrue(actionLogCaptor.getValue().getDetail().contains("new@example.com"));
  }

  @Test
  void updatePassword_shouldRejectWrongOldPassword() {
    when(passwordEncoder.matches("bad-old", "encoded-old")).thenReturn(false);

    BadRequestException ex = assertThrows(BadRequestException.class, () -> userService.updatePassword("bad-old", "new-pass"));

    assertEquals("原密码错误", ex.getMessage());
  }

  @Test
  void assertCanManageUsers_shouldRejectWithoutPermission() {
    manager.setAdminPermissions(Set.of());

    ForbiddenException ex = assertThrows(ForbiddenException.class, () -> userService.assertCanManageUsers());

    assertEquals(ErrorCode.NO_PERMISSION, ex.getErrorCode());
  }
}
