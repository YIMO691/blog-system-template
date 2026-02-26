package com.example.blog.repository;

import com.example.blog.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
  boolean existsByCommentIdAndUserId(Long commentId, Long userId);
  void deleteByCommentIdAndUserId(Long commentId, Long userId);
  void deleteByCommentIdIn(java.util.Collection<Long> commentIds);
  void deleteByUserId(Long userId);
}
