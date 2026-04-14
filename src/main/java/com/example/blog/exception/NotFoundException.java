package com.example.blog.exception;

import com.example.blog.common.api.ErrorCode;

public class NotFoundException extends RuntimeException {
  private final ErrorCode errorCode;

  public NotFoundException(String message) {
    this(ErrorCode.NOT_FOUND, message);
  }

  public NotFoundException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
