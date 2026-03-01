package com.example.blog.security;

import com.example.blog.entity.ActionLog;
import com.example.blog.entity.LoginRecord;
import com.example.blog.entity.User;
import com.example.blog.repository.ActionLogRepository;
import com.example.blog.repository.LoginRecordRepository;
import com.example.blog.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

  private final LoginRecordRepository loginRecordRepository;
  private final ActionLogRepository actionLogRepository;
  private final UserRepository userRepository;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
    try {
      String login = request.getParameter("username");
      User user = null;
      if (login != null) {
        user = userRepository.findByUsername(login)
            .or(() -> userRepository.findByEmail(login))
            .or(() -> userRepository.findByPhone(login))
            .orElse(null);
      }
      String ip = request.getRemoteAddr();
      String ua = request.getHeader("User-Agent");
      loginRecordRepository.save(LoginRecord.builder()
          .user(user) // 允许为空
          .time(Instant.now())
          .ip(ip)
          .ua(ua != null ? ua : "")
          .success(false)
          .build());
      if (user != null) {
        actionLogRepository.save(ActionLog.builder()
            .user(user)
            .time(Instant.now())
            .action("登录失败")
            .detail("IP=" + ip + "；原因：" + exception.getClass().getSimpleName())
            .build());
      }
    } catch (Exception ignored) {}
    // 回到登录页并附带错误参数
    response.sendRedirect("/auth/login?error");
  }
}
