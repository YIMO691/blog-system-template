package com.example.blog.service.impl;

import com.example.blog.service.EmailCodeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailCodeServiceImpl implements EmailCodeService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username:}")
  private String from;

  private static class Entry {
    String code;
    Instant expireAt;
    Instant lastSent;
  }

  private final Map<String, Entry> store = new ConcurrentHashMap<>();
  private final Random random = new Random();

  @PostConstruct
  void init() {}

  @Override
  public void sendCode(String email) {
    if (from == null || from.isBlank()) {
      throw new IllegalStateException("mail_unconfigured");
    }
    Entry e = store.get(email);
    Instant now = Instant.now();
    if (e != null && e.lastSent != null && now.isBefore(e.lastSent.plusSeconds(60))) {
      throw new IllegalStateException("too_frequent");
    }
    String code = String.format("%06d", random.nextInt(1_000_000));
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(email);
    message.setSubject("注册验证码");
    message.setText("您的验证码为 " + code + " ，5分钟内有效。");
    mailSender.send(message);
    Entry ne = new Entry();
    ne.code = code;
    ne.expireAt = now.plusSeconds(300);
    ne.lastSent = now;
    store.put(email, ne);
  }

  @Override
  public boolean verify(String email, String code) {
    Entry e = store.get(email);
    if (e == null) return false;
    if (Instant.now().isAfter(e.expireAt)) return false;
    return e.code != null && e.code.equalsIgnoreCase(code);
  }
}
