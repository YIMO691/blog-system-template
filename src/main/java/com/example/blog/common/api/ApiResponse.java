package com.example.blog.common.api;

public record ApiResponse<T>(
    boolean ok,
    String code,
    String message,
    T data
) {
}
