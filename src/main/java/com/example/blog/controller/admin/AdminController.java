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
  private final com.example.blog.repository.TagRepository tagRepository;

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
  public String stats(Model model, @RequestParam(defaultValue = "7") int range) {
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

    // Category Distribution
    java.util.List<Object[]> catRows = articleRepository.countGroupedByCategory();
    java.util.List<String> categoryNames = new java.util.ArrayList<>();
    java.util.List<Long> categoryCounts = new java.util.ArrayList<>();
    for (Object[] row : catRows) {
      categoryNames.add(String.valueOf(row[0] == null ? "未分类" : row[0]));
      categoryCounts.add(((Number) row[1]).longValue());
    }
    model.addAttribute("categoryNames", categoryNames);
    model.addAttribute("categoryCounts", categoryCounts);

    // Top 5 Articles by Views / Likes
    var topViews = articleRepository.findTop5ByPublishedTrueOrderByViewsDesc();
    var topLikes = articleRepository.findTop5ByPublishedTrueOrderByLikesDesc();
    model.addAttribute("topViewsTitles", topViews.stream().map(com.example.blog.entity.Article::getTitle).toList());
    model.addAttribute("topViewsValues", topViews.stream().map(a -> a.getViews()).toList());
    model.addAttribute("topLikesTitles", topLikes.stream().map(com.example.blog.entity.Article::getTitle).toList());
    model.addAttribute("topLikesValues", topLikes.stream().map(a -> a.getLikes()).toList());
    // Top commented articles
    var topCommentedRows = commentRepository.topCommentedArticles();
    var topCommentedTitles = topCommentedRows.stream().limit(5).map(r -> String.valueOf(r[0])).toList();
    var topCommentedCounts = topCommentedRows.stream().limit(5).map(r -> ((Number) r[1]).longValue()).toList();
    model.addAttribute("topCommentedTitles", topCommentedTitles);
    model.addAttribute("topCommentedCounts", topCommentedCounts);
    // Tag Top10 usage
    var tagUsageRows = tagRepository.countTagUsage();
    var tagTopNames = tagUsageRows.stream().limit(10).map(r -> String.valueOf(r[0])).toList();
    var tagTopCounts = tagUsageRows.stream().limit(10).map(r -> ((Number) r[1]).longValue()).toList();
    model.addAttribute("tagTopNames", tagTopNames);
    model.addAttribute("tagTopCounts", tagTopCounts);
    // Recent 10 articles by views
    var recentPage = articleRepository.findByPublishedTrueOrderByCreatedAtDesc(org.springframework.data.domain.PageRequest.of(0, 10));
    var recentTitles = recentPage.getContent().stream().map(com.example.blog.entity.Article::getTitle).toList();
    var recentViews = recentPage.getContent().stream().map(a -> a.getViews()).toList();
    model.addAttribute("recentTitles", recentTitles);
    model.addAttribute("recentViews", recentViews);

    // Last N Days New Articles & Comments
    int days = (range == 30) ? 30 : 7;
    java.time.ZoneId zone = java.time.ZoneId.systemDefault();
    java.time.LocalDate today = java.time.LocalDate.now(zone);
    java.util.List<String> lastDaysLabels = new java.util.ArrayList<>();
    java.util.List<Long> lastDaysArticles = new java.util.ArrayList<>();
    java.util.List<Long> lastDaysComments = new java.util.ArrayList<>();
    for (int i = days - 1; i >= 0; i--) {
      java.time.LocalDate d = today.minusDays(i);
      java.time.Instant start = d.atStartOfDay(zone).toInstant();
      java.time.Instant end = d.plusDays(1).atStartOfDay(zone).toInstant();
      long ac = articleRepository.findByCreatedAtBetween(start, end).size();
      long cc = commentRepository.findByCreatedAtBetween(start, end).size();
      lastDaysLabels.add(d.toString());
      lastDaysArticles.add(ac);
      lastDaysComments.add(cc);
    }
    model.addAttribute("range", days);
    model.addAttribute("last7DaysLabels", lastDaysLabels);
    model.addAttribute("last7Articles", lastDaysArticles);
    model.addAttribute("last7Comments", lastDaysComments);

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
