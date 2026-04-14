package com.example.blog.common.api;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  SUCCESS(HttpStatus.OK, "success"),
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "请求参数不合法"),
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "表单校验失败"),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "未登录"),
  FORBIDDEN(HttpStatus.FORBIDDEN, "无权执行该操作"),
  NO_PERMISSION(HttpStatus.FORBIDDEN, "权限不足"),
  NOT_FOUND(HttpStatus.NOT_FOUND, "资源不存在"),
  CANNOT_MODIFY_SUPER_ADMIN(HttpStatus.BAD_REQUEST, "不能修改超级管理员"),
  CANNOT_MUTE_SUPER_ADMIN(HttpStatus.BAD_REQUEST, "不能禁言超级管理员"),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "系统内部错误");

  private final HttpStatus status;
  private final String defaultMessage;

  ErrorCode(HttpStatus status, String defaultMessage) {
    this.status = status;
    this.defaultMessage = defaultMessage;
  }

  public HttpStatus status() {
    return status;
  }

  public String defaultMessage() {
    return defaultMessage;
  }
}
