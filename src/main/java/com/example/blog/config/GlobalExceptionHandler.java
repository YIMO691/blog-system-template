package com.example.blog.config;

import com.example.blog.common.api.ApiResponses;
import com.example.blog.common.api.ErrorCode;
import com.example.blog.exception.BadRequestException;
import com.example.blog.exception.ForbiddenException;
import com.example.blog.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public Object handleNotFound(NotFoundException ex,
                               Model model,
                               HttpServletRequest request,
                               HttpServletResponse response) {
    return buildResponse(ex.getErrorCode(), ex.getMessage(), "error/404", model, request, response);
  }

  @ExceptionHandler(BadRequestException.class)
  public Object handleBadRequest(BadRequestException ex,
                                 Model model,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
    return buildResponse(ex.getErrorCode(), ex.getMessage(), "error/400", model, request, response);
  }

  @ExceptionHandler(ForbiddenException.class)
  public Object handleForbidden(ForbiddenException ex,
                                Model model,
                                HttpServletRequest request,
                                HttpServletResponse response) {
    return buildResponse(ex.getErrorCode(), ex.getMessage(), "error/403", model, request, response);
  }

  @ExceptionHandler(Exception.class)
  public Object handleOther(Exception ex,
                            Model model,
                            HttpServletRequest request,
                            HttpServletResponse response) {
    return buildResponse(ErrorCode.INTERNAL_ERROR, "系统错误：" + ex.getMessage(), "error/500", model, request, response);
  }

  private Object buildResponse(ErrorCode errorCode,
                               String message,
                               String viewName,
                               Model model,
                               HttpServletRequest request,
                               HttpServletResponse response) {
    if (isApiRequest(request)) {
      return ResponseEntity.status(errorCode.status()).body(ApiResponses.error(errorCode, message));
    }
    response.setStatus(errorCode.status().value());
    model.addAttribute("message", message);
    return viewName;
  }

  private boolean isApiRequest(HttpServletRequest request) {
    String requestedWith = request.getHeader("X-Requested-With");
    if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
      return true;
    }
    String accept = request.getHeader("Accept");
    return accept != null && accept.contains("application/json");
  }
}
