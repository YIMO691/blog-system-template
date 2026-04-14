package com.example.blog.controller;

import com.example.blog.common.api.ApiResponses;
import com.example.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @PostMapping("/notifications/read-all")
  public Object readAll(HttpServletRequest request,
                        @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
    notificationService.markAllAsReadForCurrentUser();
    if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
      return ResponseEntity.ok(ApiResponses.success("已全部标记为已读", java.util.Map.of(
          "unreadCount", notificationService.countUnreadForCurrentUser()
      )));
    }
    String ref = request.getHeader("Referer");
    if (ref != null && !ref.isBlank()) {
      return "redirect:" + ref;
    }
    return "redirect:/";
  }

  @PostMapping("/notifications/delete-read")
  public Object deleteRead(HttpServletRequest request,
                           @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
    notificationService.deleteReadForCurrentUser();
    if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
      return ResponseEntity.ok(ApiResponses.success("已删除全部已读通知", java.util.Map.of(
          "unreadCount", notificationService.countUnreadForCurrentUser()
      )));
    }
    String ref = request.getHeader("Referer");
    if (ref != null && !ref.isBlank()) {
      return "redirect:" + ref;
    }
    return "redirect:/";
  }
}
