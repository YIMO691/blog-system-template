package com.example.blog.controller.admin;

import com.example.blog.common.AdminPermission;
import com.example.blog.common.api.ApiResponses;
import com.example.blog.exception.BadRequestException;
import com.example.blog.exception.ForbiddenException;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

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
    if (!canAccess(() -> userService.assertCanAccessAdmin())) {
      return "redirect:/?error=no_permission";
    }
    return "admin/dashboard";
  }

  @GetMapping("/users")
  public String users(Model model) {
    if (!canAccess(() -> userService.assertCanManageUsers())) {
      return "redirect:/admin?error=no_permission";
    }
    model.addAttribute("users", userRepository.findAll());
    return "admin/users";
  }

  @PostMapping("/users/{id}/toggle-admin")
  @ResponseBody
  public Object toggleAdmin(@PathVariable Long id,
                            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
    boolean ajaxRequest = isAjaxRequest(requestedWith);
    try {
      com.example.blog.entity.User user = userService.toggleAdminRole(id);
      if (ajaxRequest) {
        return ApiResponses.success("用户角色已更新", userStatePayload(user));
      }
      return "redirect:/admin/users";
    } catch (ForbiddenException ex) {
      if (ajaxRequest) {
        throw ex;
      }
      return "redirect:/admin?error=no_permission";
    } catch (BadRequestException ex) {
      if (ajaxRequest) {
        throw ex;
      }
      return "redirect:/admin/users?error=cannot_modify_super_admin";
    }
  }

  @PostMapping("/users/{id}/toggle-mute")
  @ResponseBody
  public Object toggleMute(@PathVariable Long id,
                           @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
    boolean ajaxRequest = isAjaxRequest(requestedWith);
    try {
      com.example.blog.entity.User user = userService.toggleMute(id);
      if (ajaxRequest) {
        return ApiResponses.success(user.isMuted() ? "用户已被禁言" : "用户已解除禁言", userStatePayload(user));
      }
      return "redirect:/admin/users";
    } catch (ForbiddenException ex) {
      if (ajaxRequest) {
        throw ex;
      }
      return "redirect:/admin?error=no_permission";
    } catch (BadRequestException ex) {
      if (ajaxRequest) {
        throw ex;
      }
      return "redirect:/admin/users?error=cannot_mute_super_admin";
    }
  }

  @GetMapping("/stats")
  public String stats(Model model, @RequestParam(defaultValue = "7") int range) {
    if (!canAccess(() -> userService.assertCanViewStats())) {
      return "redirect:/admin?error=no_permission";
    }
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
    var topViews = articleRepository.findTop5ProjectedByPublishedTrueOrderByViewsDesc(PageRequest.of(0, 5));
    var topLikes = articleRepository.findTop5ProjectedByPublishedTrueOrderByLikesDesc(PageRequest.of(0, 5));
    model.addAttribute("topViewsTitles", topViews.stream().map(com.example.blog.repository.projection.ArticleStatsProjection::getTitle).toList());
    model.addAttribute("topViewsValues", topViews.stream().map(p -> p.getViews() == null ? 0 : p.getViews()).toList());
    model.addAttribute("topLikesTitles", topLikes.stream().map(com.example.blog.repository.projection.ArticleStatsProjection::getTitle).toList());
    model.addAttribute("topLikesValues", topLikes.stream().map(p -> p.getLikes() == null ? 0 : p.getLikes()).toList());
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
    var recentArticles = articleRepository.findRecentPublishedProjected(org.springframework.data.domain.PageRequest.of(0, 10));
    var recentTitles = recentArticles.stream().map(com.example.blog.repository.projection.ArticleStatsProjection::getTitle).toList();
    var recentViews = recentArticles.stream().map(p -> p.getViews() == null ? 0 : p.getViews()).toList();
    model.addAttribute("recentTitles", recentTitles);
    model.addAttribute("recentViews", recentViews);

    // Last N Days New Articles & Comments
    int days = (range == 30) ? 30 : 7;
    ZoneId zone = ZoneId.systemDefault();
    LocalDate today = LocalDate.now(zone);
    LocalDate startDate = today.minusDays(days - 1L);
    Instant start = startDate.atStartOfDay(zone).toInstant();
    Instant end = today.plusDays(1L).atStartOfDay(zone).toInstant();

    Map<String, Long> articleCountByDate = toDateCountMap(articleRepository.countCreatedGroupedByDate(start, end));
    Map<String, Long> commentCountByDate = toDateCountMap(commentRepository.countCreatedGroupedByDate(start, end));

    java.util.List<String> lastDaysLabels = new java.util.ArrayList<>();
    java.util.List<Long> lastDaysArticles = new java.util.ArrayList<>();
    java.util.List<Long> lastDaysComments = new java.util.ArrayList<>();
    for (int i = days - 1; i >= 0; i--) {
      LocalDate d = today.minusDays(i);
      String key = d.toString();
      lastDaysLabels.add(key);
      lastDaysArticles.add(articleCountByDate.getOrDefault(key, 0L));
      lastDaysComments.add(commentCountByDate.getOrDefault(key, 0L));
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

  private Map<String, Long> toDateCountMap(java.util.List<Object[]> rows) {
    Map<String, Long> result = new HashMap<>();
    for (Object[] row : rows) {
      if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
        continue;
      }
      result.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
    }
    return result;
  }

  private java.util.Map<String, Object> userStatePayload(com.example.blog.entity.User user) {
    return java.util.Map.of(
        "ok", true,
        "id", user.getId(),
        "isAdmin", user.isAdmin(),
        "muted", user.isMuted(),
        "superAdmin", user.isSuperAdmin(),
        "permissionLabels", user.getAdminPermissions().stream()
            .map(AdminPermission::getLabel)
            .toList()
    );
  }

  @GetMapping("/articles")
  public String articles(Model model, @RequestParam(defaultValue = "0") int page) {
    if (!canAccess(() -> userService.assertCanManageArticles())) {
      return "redirect:/admin?error=no_permission";
    }
    model.addAttribute("page", articleRepository.findAll(PageRequest.of(page, 20)));
    return "admin/articles";
  }

  @PostMapping("/articles/{id}/delete")
  @ResponseBody
  public Object deleteArticle(@PathVariable Long id,
                              @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
    boolean ajaxRequest = isAjaxRequest(requestedWith);
    try {
      userService.assertCanManageArticles();
      articleService.delete(id);
      if (ajaxRequest) {
        return ApiResponses.success("文章已删除", java.util.Map.of("id", id));
      }
      return "redirect:/admin/articles";
    } catch (ForbiddenException ex) {
      if (ajaxRequest) {
        throw ex;
      }
      return "redirect:/admin?error=no_permission";
    }
  }

  @GetMapping("/comments")
  public String comments(Model model) {
    if (!canAccess(() -> userService.assertCanModerateComments())) {
      return "redirect:/admin?error=no_permission";
    }
    model.addAttribute("pending", commentService.listPending());
    return "admin/comments";
  }

  @PostMapping("/comments/{id}/approve")
  @ResponseBody
  public Object approve(@PathVariable Long id,
                        @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
    boolean ajaxRequest = isAjaxRequest(requestedWith);
    try {
      userService.assertCanModerateComments();
      commentService.approve(id);
      if (ajaxRequest) {
        return ApiResponses.successMessage("评论已通过审核");
      }
      return "redirect:/admin/comments";
    } catch (ForbiddenException ex) {
      if (ajaxRequest) {
        throw ex;
      }
      return "redirect:/admin?error=no_permission";
    }
  }

  @PostMapping("/comments/{id}/delete")
  @ResponseBody
  public Object delete(@PathVariable Long id,
                       @RequestParam(required = false) String redirect,
                       @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
    boolean ajaxRequest = isAjaxRequest(requestedWith);
    try {
      userService.assertCanModerateComments();
      commentService.delete(id);
      if (ajaxRequest) {
        return ApiResponses.successMessage("评论已删除");
      }
      if (redirect != null && !redirect.isBlank()) {
        return "redirect:" + redirect;
      }
      return "redirect:/admin/comments";
    } catch (ForbiddenException ex) {
      if (ajaxRequest) {
        throw ex;
      }
      return "redirect:/admin?error=no_permission";
    }
  }

  @GetMapping("/notifications")
  public String notifications(Model model) {
    if (!canAccess(() -> userService.assertCanManageAdminNotifications())) {
      return "redirect:/admin?error=no_permission";
    }
    model.addAttribute("notices", notificationService.listForCurrentUser());
    return "admin/notifications";
  }

  @PostMapping("/notifications/{id}/read")
  @ResponseBody
  public Object read(@PathVariable Long id,
                     @RequestParam(required = false) String redirect,
                     @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
    boolean ajaxRequest = isAjaxRequest(requestedWith);
    try {
      userService.assertCanManageAdminNotifications();
      notificationService.markAsRead(id);
      if (ajaxRequest) {
        return ApiResponses.success("通知已标记为已读", java.util.Map.of(
            "unreadCount", notificationService.countUnreadForCurrentUser()
        ));
      }
      if (redirect != null && !redirect.isBlank()) {
        return "redirect:" + redirect;
      }
      return "redirect:/admin/notifications";
    } catch (ForbiddenException ex) {
      if (ajaxRequest) {
        throw ex;
      }
      return "redirect:/admin?error=no_permission";
    }
  }

  private boolean isAjaxRequest(String requestedWith) {
    return "XMLHttpRequest".equalsIgnoreCase(requestedWith);
  }

  private boolean canAccess(Runnable permissionCheck) {
    try {
      permissionCheck.run();
      return true;
    } catch (ForbiddenException ex) {
      return false;
    }
  }
}
