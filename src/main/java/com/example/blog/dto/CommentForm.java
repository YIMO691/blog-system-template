package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentForm(
    @NotBlank @Size(max = 500) String content,
    Long parentId
) {}
