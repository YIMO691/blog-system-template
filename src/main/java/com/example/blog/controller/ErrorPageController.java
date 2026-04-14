package com.example.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController {

  @GetMapping("/error/403")
  public String forbidden(Model model) {
    if (!model.containsAttribute("message")) {
      model.addAttribute("message", "你没有权限访问该页面或执行该操作。");
    }
    return "error/403";
  }
}
