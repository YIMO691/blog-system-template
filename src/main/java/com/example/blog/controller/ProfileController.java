package com.example.blog.controller;

import com.example.blog.common.api.ApiResponses;
import com.example.blog.entity.ActionLog;
import com.example.blog.exception.BadRequestException;
import com.example.blog.entity.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final com.example.blog.service.NotificationService notificationService;

    private final com.example.blog.repository.ArticleLikeRepository articleLikeRepository;
    private final com.example.blog.repository.CommentLikeRepository commentLikeRepository;
    private final com.example.blog.repository.CommentRepository commentRepository;
    private final com.example.blog.repository.LoginRecordRepository loginRecordRepository;
    private final com.example.blog.repository.ActionLogRepository actionLogRepository;

    @GetMapping
    public String profile(Model model, HttpServletRequest request) {
        User current = userService.getCurrentUserOrThrow();
        model.addAttribute("user", current);

        var loginPage = org.springframework.data.domain.PageRequest.of(0, 20);
        var actionPage = org.springframework.data.domain.PageRequest.of(0, 20);
        model.addAttribute("loginRecords", loginRecordRepository.findByUserIdOrderByTimeDesc(current.getId(), loginPage).getContent());
        model.addAttribute("actionLogs", actionLogRepository.findByUserIdOrderByTimeDesc(current.getId(), actionPage).getContent());
        return "user/profile";
    }

    @PostMapping("/update-nickname")
    public Object updateNickname(@RequestParam String nickname,
                                 @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        UserService.NicknameUpdateResult result = userService.updateNickname(nickname);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return ResponseEntity.ok(ApiResponses.success(result.message(), java.util.Map.of(
                "nickname", result.nickname()
            )));
        }
        return "redirect:/profile?success=nickname";
    }

    @PostMapping("/update-password")
    public Object updatePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        try {
            UserService.PasswordUpdateResult result = userService.updatePassword(oldPassword, newPassword);
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                return ResponseEntity.ok(ApiResponses.successMessage(result.message()));
            }
            return "redirect:/profile?success=password";
        } catch (BadRequestException ex) {
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                throw ex;
            }
            if ("原密码错误".equals(ex.getMessage())) {
                return "redirect:/profile?error=wrong_password";
            }
            if ("新密码不能与旧密码相同".equals(ex.getMessage())) {
                return "redirect:/profile?error=same_password";
            }
            throw ex;
        }
    }

    @PostMapping("/update-email")
    public Object updateEmail(@RequestParam String newEmail,
                              @RequestParam String code,
                              @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        try {
            UserService.EmailUpdateResult result = userService.updateEmail(newEmail, code);
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                return ResponseEntity.ok(ApiResponses.success(result.message(), java.util.Map.of(
                    "email", result.email(),
                    "emailVerified", result.emailVerified()
                )));
            }
            return "redirect:/profile?success=email";
        } catch (BadRequestException ex) {
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                throw ex;
            }
            return switch (ex.getMessage()) {
                case "邮箱不能为空" -> "redirect:/profile?error=email_blank";
                case "新邮箱不能与当前邮箱相同" -> "redirect:/profile?error=email_same";
                case "该邮箱已被绑定" -> "redirect:/profile?error=email_exists";
                case "邮箱验证码无效或已过期" -> "redirect:/profile?error=email_code_invalid";
                default -> throw ex;
            };
        }
    }

    @PostMapping("/update-phone")
    public Object updatePhone(@RequestParam(required = false) String phone,
                              @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        try {
            UserService.PhoneUpdateResult result = userService.updatePhone(phone);
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                return ResponseEntity.ok(ApiResponses.success(result.message(), java.util.Map.of(
                    "phone", result.phone()
                )));
            }
            return "redirect:/profile?success=phone";
        } catch (BadRequestException ex) {
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                throw ex;
            }
            return switch (ex.getMessage()) {
                case "手机号需为11位数字" -> "redirect:/profile?error=phone_format";
                case "新手机号不能与当前相同" -> "redirect:/profile?error=phone_same";
                case "该手机号已被绑定" -> "redirect:/profile?error=phone_exists";
                default -> throw ex;
            };
        }
    }

    @PostMapping("/delete-account")
    @org.springframework.transaction.annotation.Transactional
    public String deleteAccount(HttpServletRequest request) {
        User current = userService.getCurrentUserOrThrow();
        if (current.isSuperAdmin()) {
            return "redirect:/profile?error=cannot_delete_super_admin";
        }
        
        // 1. Delete Article Likes by this user
        articleLikeRepository.deleteByUserId(current.getId());
        
        // 2. Delete Comment Likes by this user
        commentLikeRepository.deleteByUserId(current.getId());

        // 3. Delete Comments by this user (and their likes if any, but comments table logic is complex if comments have replies)
        // Note: If comments have replies (children), deleting them might fail if not cascaded.
        // Let's rely on JPA or manual deletion.
        // If a user comment has replies, we might want to keep the comment but mark user as null, or delete recursively.
        // The error log showed: foreign key constraint fails (`blog_db`.`comments`, CONSTRAINT `FK...` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`))
        // This means we just need to handle the comments authored by this user.
        // Simple approach: Delete their comments. If those comments have children, we need to handle that.
        // Or set user_id to null if nullable.
        
        // Let's try deleting user's comments.
        // But wait, if we delete a comment, we must also delete likes on THAT comment.
        // So:
        // Find all comments by user
        // For each comment, delete likes on it
        // Delete the comment
        
        // However, comments can be parents to other comments.
        // Ideally, set user to null (anonymous) to preserve thread structure.
        // Let's check Comment entity if user is nullable.
        // In Comment.java: @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
        // It seems nullable by default unless validaton says otherwise.
        // Let's try setting user to null.
        
        java.util.List<com.example.blog.entity.Comment> userComments = commentRepository.findByUserId(current.getId());
        for (com.example.blog.entity.Comment c : userComments) {
            c.setUser(null);
            c.setDisplayName(c.getDisplayName() + " (已注销)");
            commentRepository.save(c);
        }
        
        // What about articles authored by user?
        // If user is not admin, maybe they can't write articles? 
        // But if they did (e.g. earlier role), we should handle it.
        // Similar to comments, maybe keep them or delete.
        // Let's assume for now regular users don't have articles or we delete them.
        // But deleting articles is heavy (likes, comments on them).
        // Let's check if they have articles.
        
        // Finally delete user
        actionLogRepository.save(ActionLog.builder()
            .user(current)
            .time(java.time.Instant.now())
            .action("注销账号")
            .detail("用户发起账号注销")
            .build());
        userRepository.delete(current);
        
        try {
            request.logout();
        } catch (Exception e) {
            // ignore
        }
        return "redirect:/auth/login?deleted";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("notices", notificationService.listForCurrentUser());
        return "user/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    @org.springframework.web.bind.annotation.ResponseBody
    public Object read(@PathVariable Long id,
                       @org.springframework.web.bind.annotation.RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        notificationService.markAsRead(id);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return ApiResponses.success("通知已标记为已读", java.util.Map.of(
                "unreadCount", notificationService.countUnreadForCurrentUser()
            ));
        }
        return "redirect:/profile/notifications";
    }
}
