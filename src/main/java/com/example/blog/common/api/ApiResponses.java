package com.example.blog.common.api;

import org.springframework.http.ResponseEntity;

public final class ApiResponses {

  private ApiResponses() {
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, ErrorCode.SUCCESS.name(), ErrorCode.SUCCESS.defaultMessage(), data);
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(true, ErrorCode.SUCCESS.name(), message, data);
  }

  public static ApiResponse<Void> successMessage(String message) {
    return success(message, null);
  }

  public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
    return new ApiResponse<>(false, errorCode.name(), message, null);
  }

  public static ApiResponse<Void> error(ErrorCode errorCode) {
    return error(errorCode, errorCode.defaultMessage());
  }

  public static ResponseEntity<ApiResponse<Void>> errorResponse(ErrorCode errorCode, String message) {
    return ResponseEntity.status(errorCode.status()).body(error(errorCode, message));
  }

  public static ResponseEntity<ApiResponse<Void>> errorResponse(ErrorCode errorCode) {
    return errorResponse(errorCode, errorCode.defaultMessage());
  }
}
