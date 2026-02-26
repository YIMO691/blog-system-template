package com.example.blog.controller;

import com.example.blog.dto.RegisterRequest;
import com.example.blog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;

  @GetMapping("/login")
  public String login() {
    return "auth/login";
  }

  @GetMapping("/register")
  public String registerForm(Model model) {
    model.addAttribute("form", new RegisterRequest(null, null, null));
    return "auth/register";
  }

  @PostMapping("/register")
  public String register(@ModelAttribute("form") @Valid RegisterRequest form, BindingResult br) {
    if (br.hasErrors()) return "auth/register";
    try {
        userService.register(form);
    } catch (com.example.blog.exception.BadRequestException e) {
        br.rejectValue("username", "error.username", e.getMessage());
        return "auth/register";
    }
    return "redirect:/auth/login?registered";
  }
}
