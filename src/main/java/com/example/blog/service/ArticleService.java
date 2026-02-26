package com.example.blog.service;

import com.example.blog.dto.ArticleForm;
import com.example.blog.entity.Article;
import org.springframework.data.domain.Page;

public interface ArticleService {
  Page<Article> listPublished(int page, int size);
  Page<Article> searchPublished(String keyword, int page, int size);
  Page<Article> listPublishedByCategory(Long categoryId, int page, int size);
  Page<Article> listPublishedByTag(String tagName, int page, int size);
  Article getPublishedBySlug(String slug);

  Article getById(Long id);

  Article createOrUpdate(Long id, ArticleForm form);
  void delete(Long id);

  void increaseViewCount(Long id);
  void toggleLike(Long id);
  boolean isLikedByCurrentUser(Long id);

  java.util.List<Article> listDrafts(Long authorId);
}
