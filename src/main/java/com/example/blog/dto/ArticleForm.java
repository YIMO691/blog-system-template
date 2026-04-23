package com.example.blog.dto;

import com.example.blog.common.ArticleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ArticleForm(
    @NotBlank @Size(max = 200) String title,
    @NotBlank String content,
    ArticleStatus status,
    Long categoryId,
    String newCategory, // for creating a new category
    Set<String> tags // tag names
) {}
