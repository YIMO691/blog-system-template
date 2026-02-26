package com.example.blog.repository;

import com.example.blog.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
  boolean existsByArticleIdAndUserId(Long articleId, Long userId);
  void deleteByArticleIdAndUserId(Long articleId, Long userId);
  void deleteByArticleId(Long articleId);
  void deleteByUserId(Long userId);
}
