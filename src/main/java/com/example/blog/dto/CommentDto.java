package com.example.blog.dto;

import java.time.Instant;
import java.util.List;

public record CommentDto(
    Long id,
    String displayName,
    String content,
    Instant createdAt,
    int likes,
    boolean likedByCurrentUser,
    List<CommentDto> replies
) {}
