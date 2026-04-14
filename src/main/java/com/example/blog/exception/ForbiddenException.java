package com.example.blog.exception;

import com.example.blog.common.api.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
  private final ErrorCode errorCode;

  public ForbiddenException(String message) {
    this(ErrorCode.FORBIDDEN, message);
  }

  public ForbiddenException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
