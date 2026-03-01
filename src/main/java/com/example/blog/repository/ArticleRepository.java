package com.example.blog.repository;

import com.example.blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

  @EntityGraph(attributePaths = {"category", "author"})
  Page<Article> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

  @EntityGraph(attributePaths = {"category", "author"})
  Optional<Article> findBySlugAndPublishedTrue(String slug);

  // basic search
  @EntityGraph(attributePaths = {"category", "author"})
  Page<Article> findByPublishedTrueAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword, Pageable pageable);

  // Specification + EntityGraph 支持（用于标题或正文内容联合搜索）
  @EntityGraph(attributePaths = {"category", "author"})
  Page<Article> findAll(Specification<Article> spec, Pageable pageable);

  @EntityGraph(attributePaths = {"category", "author"})
  Page<Article> findByPublishedTrueAndCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

  @EntityGraph(attributePaths = {"category", "author"})
  Page<Article> findByPublishedTrueAndTagsNameOrderByCreatedAtDesc(String tagName, Pageable pageable);

  @EntityGraph(attributePaths = {"category", "author"})
  java.util.List<Article> findByAuthorIdAndPublishedFalseOrderByCreatedAtDesc(Long authorId);

  long countByPublishedTrue();
  long countByPublishedFalse();

  @org.springframework.data.jpa.repository.Query("SELECT SUM(a.views) FROM Article a")
  Long sumViews();

  @org.springframework.data.jpa.repository.Query("SELECT SUM(a.likes) FROM Article a")
  Long sumLikes();

  java.util.List<Article> findTop5ByPublishedTrueOrderByViewsDesc();
  java.util.List<Article> findTop5ByPublishedTrueOrderByLikesDesc();

  @org.springframework.data.jpa.repository.Query("SELECT COALESCE(c.name, '未分类') AS name, COUNT(a) FROM Article a LEFT JOIN a.category c GROUP BY c.name ORDER BY COUNT(a) DESC")
  java.util.List<Object[]> countGroupedByCategory();

  java.util.List<Article> findByCreatedAtBetween(java.time.Instant start, java.time.Instant end);
}
