package com.example.blog.config;

import com.example.blog.exception.BadRequestException;
import com.example.blog.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleNotFound(NotFoundException ex, Model model) {
    model.addAttribute("message", ex.getMessage());
    return "error/404";
  }

  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String handleBadRequest(BadRequestException ex, Model model) {
    model.addAttribute("message", ex.getMessage());
    return "error/400";
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String handleOther(Exception ex, Model model) {
    model.addAttribute("message", "系统错误：" + ex.getMessage());
    return "error/500";
  }
}
