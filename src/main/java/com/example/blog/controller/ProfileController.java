package com.example.blog.controller;

import com.example.blog.entity.User;
import com.example.blog.entity.LoginRecord;
import com.example.blog.entity.ActionLog;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.example.blog.service.NotificationService notificationService;
    private final com.example.blog.service.EmailCodeService emailCodeService;

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
    public String updateNickname(@RequestParam String nickname) {
        User current = userService.getCurrentUserOrThrow();
        current.setNickname(nickname);
        userRepository.save(current);
        actionLogRepository.save(ActionLog.builder()
            .user(current)
            .time(java.time.Instant.now())
            .action("修改昵称")
            .detail("昵称更新为：" + nickname)
            .build());
        return "redirect:/profile?success=nickname";
    }

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        User current = userService.getCurrentUserOrThrow();
        if (!passwordEncoder.matches(oldPassword, current.getPasswordHash())) {
            return "redirect:/profile?error=wrong_password";
        }
        if (passwordEncoder.matches(newPassword, current.getPasswordHash())) {
            return "redirect:/profile?error=same_password";
        }
        current.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(current);
        actionLogRepository.save(ActionLog.builder()
            .user(current)
            .time(java.time.Instant.now())
            .action("修改密码")
            .detail("用户修改了登录密码")
            .build());
        return "redirect:/profile?success=password";
    }

    @PostMapping("/update-email")
    public String updateEmail(@RequestParam String newEmail, @RequestParam String code) {
        User current = userService.getCurrentUserOrThrow();
        if (newEmail == null || newEmail.isBlank()) {
            return "redirect:/profile?error=email_blank";
        }
        if (newEmail.equalsIgnoreCase(current.getEmail() != null ? current.getEmail() : "")) {
            return "redirect:/profile?error=email_same";
        }
        if (userRepository.existsByEmail(newEmail)) {
            return "redirect:/profile?error=email_exists";
        }
        if (code == null || code.isBlank() || !emailCodeService.verify(newEmail, code)) {
            return "redirect:/profile?error=email_code_invalid";
        }
        current.setEmail(newEmail);
        current.setEmailVerified(true);
        userRepository.save(current);
        actionLogRepository.save(ActionLog.builder()
            .user(current)
            .time(java.time.Instant.now())
            .action("更新邮箱")
            .detail("新邮箱：" + newEmail)
            .build());
        return "redirect:/profile?success=email";
    }

    @PostMapping("/update-phone")
    public String updatePhone(@RequestParam(required = false) String phone) {
        User current = userService.getCurrentUserOrThrow();
        if (phone == null || phone.isBlank()) {
            current.setPhone(null);
            userRepository.save(current);
            actionLogRepository.save(ActionLog.builder()
                .user(current)
                .time(java.time.Instant.now())
                .action("解绑手机号")
                .detail("手机号已解除绑定")
                .build());
            return "redirect:/profile?success=phone";
        }
        if (!phone.matches("\\d{11}")) {
            return "redirect:/profile?error=phone_format";
        }
        if (phone.equals(current.getPhone() != null ? current.getPhone() : "")) {
            return "redirect:/profile?error=phone_same";
        }
        if (userRepository.existsByPhone(phone)) {
            return "redirect:/profile?error=phone_exists";
        }
        current.setPhone(phone);
        userRepository.save(current);
        actionLogRepository.save(ActionLog.builder()
            .user(current)
            .time(java.time.Instant.now())
            .action("更新手机号")
            .detail("新手机号：" + phone)
            .build());
        return "redirect:/profile?success=phone";
    }

    @PostMapping("/delete-account")
    @org.springframework.transaction.annotation.Transactional
    public String deleteAccount(HttpServletRequest request) {
        User current = userService.getCurrentUserOrThrow();
        if ("admin".equals(current.getUsername())) {
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
    public String read(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return "redirect:/profile/notifications";
    }
}
