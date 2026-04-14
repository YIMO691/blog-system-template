package com.example.blog.service.impl;

import com.example.blog.common.NotificationType;
import com.example.blog.common.Role;
import com.example.blog.entity.Notification;
import com.example.blog.entity.User;
import com.example.blog.exception.NotFoundException;
import com.example.blog.repository.NotificationRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Override
  public Notification notifyUser(Long userId, NotificationType type, String message, String link) {
    User u = userRepository.findById(userId).orElse(null);
    if (u == null) return null;
    Notification n = Notification.builder()
        .type(type)
        .recipient(u)
        .message(message)
        .link(link)
        .read(false)
        .build();
    return notificationRepository.save(n);
  }

  @Override
  public Notification notifyAdmin(NotificationType type, String message, String link) {
    java.util.List<User> recipients = userRepository.findByRole(Role.ROLE_ADMIN).stream()
        .filter(admin -> switch (type) {
          case COMMENT_PENDING -> admin.canModerateComments();
          default -> admin.canManageAdminNotifications();
        })
        .toList();

    Notification first = null;
    for (User admin : recipients) {
      Notification n = Notification.builder()
          .type(type)
          .recipient(admin)
          .message(message)
          .link(link)
          .read(false)
          .build();
      Notification saved = notificationRepository.save(n);
      if (first == null) {
        first = saved;
      }
    }
    return first;
  }

  @Override
  public List<Notification> listForCurrentUser() {
    User current = getCurrentUserOrThrow();
    return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(current.getId());
  }

  @Override
  public long countUnreadForCurrentUser() {
    User current = getCurrentUserOrThrow();
    return notificationRepository.countByRecipientIdAndReadFalse(current.getId());
  }

  @Override
  public void markAsRead(Long id) {
    Notification n = notificationRepository.findById(id).orElse(null);
    if (n == null) return;
    n.setRead(true);
    notificationRepository.save(n);
  }

  @Override
  @org.springframework.transaction.annotation.Transactional
  public void markAllAsReadForCurrentUser() {
    User current = getCurrentUserOrThrow();
    notificationRepository.markAllReadByRecipientId(current.getId());
  }

  @Override
  @org.springframework.transaction.annotation.Transactional
  public void deleteReadForCurrentUser() {
    User current = getCurrentUserOrThrow();
    notificationRepository.deleteReadByRecipientId(current.getId());
  }

  private User getCurrentUserOrThrow() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
      throw new NotFoundException("未登录");
    }
    return userRepository.findByUsername(auth.getName())
        .orElseThrow(() -> new NotFoundException("用户不存在"));
  }
}
