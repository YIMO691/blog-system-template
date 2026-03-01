package com.example.blog.repository;

import com.example.blog.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  List<Comment> findByArticleIdAndApprovedTrueOrderByCreatedAtAsc(Long articleId);
  List<Comment> findByApprovedFalseOrderByCreatedAtDesc();
  org.springframework.data.domain.Page<Comment> findByApprovedTrueOrderByCreatedAtDesc(org.springframework.data.domain.Pageable pageable);
  List<Comment> findByArticleId(Long articleId);
  List<Comment> findByUserId(Long userId);
  void deleteByArticleId(Long articleId);
  void deleteByUserId(Long userId);

  long countByApprovedTrue();
  long countByApprovedFalse();

  @org.springframework.data.jpa.repository.Query("""
      SELECT c.article.title, COUNT(c)
      FROM Comment c
      WHERE c.approved = true AND c.article IS NOT NULL AND c.article.published = true
      GROUP BY c.article.title
      ORDER BY COUNT(c) DESC
      """)
  java.util.List<Object[]> topCommentedArticles();

  java.util.List<Comment> findByCreatedAtBetween(java.time.Instant start, java.time.Instant end);
}
