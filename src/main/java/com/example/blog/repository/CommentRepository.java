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
}
