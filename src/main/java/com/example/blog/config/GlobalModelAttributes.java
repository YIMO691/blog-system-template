package com.example.blog.config;

import com.example.blog.entity.Notification;
import com.example.blog.service.UserService;
import com.example.blog.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

  private final NotificationRepository notificationRepository;
  private final UserService userService;

  @ModelAttribute("notificationsUnread")
  public Long notificationsUnread() {
    try {
      Long userId = userService.getCurrentUserOrThrow().getId();
      return notificationRepository.countByRecipientIdAndReadFalse(userId);
    } catch (Exception e) {
      return 0L;
    }
  }

  @ModelAttribute("notificationsRecent")
  public List<Notification> notificationsRecent() {
    try {
      Long userId = userService.getCurrentUserOrThrow().getId();
      List<Notification> all = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
      return all.size() > 5 ? all.subList(0, 5) : all;
    } catch (Exception e) {
      return java.util.Collections.emptyList();
    }
  }
}
