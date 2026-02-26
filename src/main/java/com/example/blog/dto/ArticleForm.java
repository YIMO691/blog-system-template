package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ArticleForm(
    @NotBlank @Size(max = 200) String title,
    @NotBlank String content,
    boolean published,
    Long categoryId,
    String newCategory, // for creating a new category
    Set<String> tags // tag names
) {}
