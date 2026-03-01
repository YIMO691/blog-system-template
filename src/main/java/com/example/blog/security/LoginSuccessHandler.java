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
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  private final LoginRecordRepository loginRecordRepository;
  private final ActionLogRepository actionLogRepository;
  private final UserRepository userRepository;

  {
    setDefaultTargetUrl("/");
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    try {
      String username = authentication.getName();
      User user = userRepository.findByUsername(username).orElse(null);
      if (user == null) {
        user = userRepository.findByEmail(username).orElse(null);
      }
      if (user == null) {
        user = userRepository.findByPhone(username).orElse(null);
      }
      String ip = request.getRemoteAddr();
      String ua = request.getHeader("User-Agent");
      loginRecordRepository.save(LoginRecord.builder()
          .user(user)
          .time(Instant.now())
          .ip(ip)
          .ua(ua != null ? ua : "")
          .success(true)
          .build());
      if (user != null) {
        actionLogRepository.save(ActionLog.builder()
            .user(user)
            .time(Instant.now())
            .action("登录成功")
            .detail("IP=" + ip)
            .build());
      }
    } catch (Exception ignored) {}
    super.onAuthenticationSuccess(request, response, authentication);
  }
}
