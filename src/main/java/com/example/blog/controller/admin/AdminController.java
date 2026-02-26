package com.example.blog.controller.admin;

import com.example.blog.repository.ArticleRepository;
import com.example.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final ArticleRepository articleRepository;
  private final com.example.blog.service.ArticleService articleService;
  private final CommentService commentService;
  private final com.example.blog.repository.CommentRepository commentRepository;
  private final com.example.blog.repository.UserRepository userRepository;
  private final com.example.blog.service.UserService userService;
  private final com.example.blog.service.NotificationService notificationService;

  @GetMapping
  public String dashboard() {
    return "admin/dashboard";
  }

  @GetMapping("/users")
  public String users(Model model) {
    if (!"admin".equals(userService.getCurrentUserOrThrow().getUsername())) {
      return "redirect:/admin?error=no_permission";
    }
    model.addAttribute("users", userRepository.findAll());
    return "admin/users";
  }

  @PostMapping("/users/{id}/toggle-admin")
  public String toggleAdmin(@PathVariable Long id) {
    if (!"admin".equals(userService.getCurrentUserOrThrow().getUsername())) {
      return "redirect:/admin?error=no_permission";
    }
    com.example.blog.entity.User u = userRepository.findById(id).orElseThrow();
    if ("admin".equals(u.getUsername())) {
      return "redirect:/admin/users?error=cannot_modify_super_admin";
    }

    if (u.getRole() == com.example.blog.common.Role.ROLE_ADMIN) {
      u.setRole(com.example.blog.common.Role.ROLE_USER);
    } else {
      u.setRole(com.example.blog.common.Role.ROLE_ADMIN);
    }
    userRepository.save(u);
    notificationService.notifyUser(
        u.getId(),
        com.example.blog.common.NotificationType.USER_ROLE_CHANGED,
        "你的用户角色已更新",
        "/profile"
    );
    return "redirect:/admin/users";
  }

  @PostMapping("/users/{id}/toggle-mute")
  public String toggleMute(@PathVariable Long id) {
    if (!"admin".equals(userService.getCurrentUserOrThrow().getUsername())) {
      return "redirect:/admin?error=no_permission";
    }
    com.example.blog.entity.User u = userRepository.findById(id).orElseThrow();
    if ("admin".equals(u.getUsername())) {
      return "redirect:/admin/users?error=cannot_mute_super_admin";
    }

    u.setMuted(!u.isMuted());
    userRepository.save(u);
    notificationService.notifyUser(
        u.getId(),
        u.isMuted() ? com.example.blog.common.NotificationType.USER_MUTED : com.example.blog.common.NotificationType.USER_UNMUTED,
        u.isMuted() ? "你的账号已被禁言" : "你的账号已解除禁言",
        "/profile"
    );
    return "redirect:/admin/users";
  }

  @GetMapping("/stats")
  public String stats(Model model) {
    // Article Stats
    model.addAttribute("articleCount", articleRepository.count());
    model.addAttribute("articlePublishedCount", articleRepository.countByPublishedTrue());
    model.addAttribute("articleDraftCount", articleRepository.countByPublishedFalse());
    model.addAttribute("totalViews", articleRepository.sumViews());
    model.addAttribute("totalLikes", articleRepository.sumLikes());

    // Comment Stats
    model.addAttribute("commentCount", commentRepository.count());
    model.addAttribute("commentApprovedCount", commentRepository.countByApprovedTrue());
    model.addAttribute("commentPendingCount", commentRepository.countByApprovedFalse());

    // User Stats
    model.addAttribute("userCount", userRepository.count());

    // System Info
    model.addAttribute("osName", System.getProperty("os.name"));
    model.addAttribute("javaVersion", System.getProperty("java.version"));
    model.addAttribute("jvmMemory", Runtime.getRuntime().totalMemory() / 1024 / 1024 + " MB");
    model.addAttribute("jvmFreeMemory", Runtime.getRuntime().freeMemory() / 1024 / 1024 + " MB");

    return "admin/stats";
  }

  @GetMapping("/articles")
  public String articles(Model model, @RequestParam(defaultValue = "0") int page) {
    model.addAttribute("page", articleRepository.findAll(PageRequest.of(page, 20)));
    return "admin/articles";
  }

  @PostMapping("/articles/{id}/delete")
  public String deleteArticle(@PathVariable Long id) {
    articleService.delete(id);
    return "redirect:/admin/articles";
  }

  @GetMapping("/comments")
  public String comments(Model model) {
    model.addAttribute("pending", commentService.listPending());
    return "admin/comments";
  }

  @PostMapping("/comments/{id}/approve")
  public String approve(@PathVariable Long id) {
    commentService.approve(id);
    return "redirect:/admin/comments";
  }

  @PostMapping("/comments/{id}/delete")
  public String delete(@PathVariable Long id, @RequestParam(required = false) String redirect) {
    commentService.delete(id);
    if (redirect != null && !redirect.isBlank()) {
      return "redirect:" + redirect;
    }
    return "redirect:/admin/comments";
  }

  @GetMapping("/notifications")
  public String notifications(Model model) {
    model.addAttribute("notices", notificationService.listForCurrentUser());
    return "admin/notifications";
  }

  @PostMapping("/notifications/{id}/read")
  public String read(@PathVariable Long id, @RequestParam(required = false) String redirect) {
    notificationService.markAsRead(id);
    if (redirect != null && !redirect.isBlank()) {
      return "redirect:" + redirect;
    }
    return "redirect:/admin/notifications";
  }
}
