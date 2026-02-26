package com.example.blog.service;

import com.example.blog.common.NotificationType;
import com.example.blog.entity.Notification;

import java.util.List;

public interface NotificationService {
  Notification notifyUser(Long userId, NotificationType type, String message, String link);
  Notification notifyAdmin(NotificationType type, String message, String link);
  List<Notification> listForCurrentUser();
  void markAsRead(Long id);
  void markAllAsReadForCurrentUser();
  void deleteReadForCurrentUser();
}
