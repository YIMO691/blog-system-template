package com.example.blog.service.impl;

import com.example.blog.common.NotificationType;
import com.example.blog.common.Role;
import com.example.blog.entity.Notification;
import com.example.blog.entity.User;
import com.example.blog.repository.NotificationRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.NotificationService;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final UserService userService;

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
    User current = userService.getCurrentUserOrThrow();
    return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(current.getId());
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
    User current = userService.getCurrentUserOrThrow();
    notificationRepository.markAllReadByRecipientId(current.getId());
  }

  @Override
  @org.springframework.transaction.annotation.Transactional
  public void deleteReadForCurrentUser() {
    User current = userService.getCurrentUserOrThrow();
    notificationRepository.deleteReadByRecipientId(current.getId());
  }
}
