package com.example.blog.exception;

import com.example.blog.common.api.ErrorCode;

public class BadRequestException extends RuntimeException {
  private final ErrorCode errorCode;

  public BadRequestException(String message) {
    this(ErrorCode.BAD_REQUEST, message);
  }

  public BadRequestException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
