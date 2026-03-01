package com.example.blog.service;

public interface EmailCodeService {
  void sendCode(String email);
  boolean verify(String email, String code);
}
