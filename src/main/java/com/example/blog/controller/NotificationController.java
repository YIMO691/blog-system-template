package com.example.blog.controller;

import com.example.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @PostMapping("/notifications/read-all")
  public String readAll(HttpServletRequest request) {
    notificationService.markAllAsReadForCurrentUser();
    String ref = request.getHeader("Referer");
    if (ref != null && !ref.isBlank()) {
      return "redirect:" + ref;
    }
    return "redirect:/";
  }

  @PostMapping("/notifications/delete-read")
  public String deleteRead(HttpServletRequest request) {
    notificationService.deleteReadForCurrentUser();
    String ref = request.getHeader("Referer");
    if (ref != null && !ref.isBlank()) {
      return "redirect:" + ref;
    }
    return "redirect:/";
  }
}
