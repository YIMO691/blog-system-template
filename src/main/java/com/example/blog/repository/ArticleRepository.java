package com.example.blog.repository;

import com.example.blog.entity.Article;
import com.example.blog.repository.projection.ArticleStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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

  @org.springframework.data.jpa.repository.Query("""
      SELECT a.title AS title, a.views AS views, a.likes AS likes
      FROM Article a
      WHERE a.published = true
      ORDER BY a.views DESC, a.createdAt DESC
      """)
  java.util.List<ArticleStatsProjection> findTop5ProjectedByPublishedTrueOrderByViewsDesc(Pageable pageable);

  @org.springframework.data.jpa.repository.Query("""
      SELECT a.title AS title, a.views AS views, a.likes AS likes
      FROM Article a
      WHERE a.published = true
      ORDER BY a.likes DESC, a.createdAt DESC
      """)
  java.util.List<ArticleStatsProjection> findTop5ProjectedByPublishedTrueOrderByLikesDesc(Pageable pageable);

  @org.springframework.data.jpa.repository.Query("SELECT COALESCE(c.name, '未分类') AS name, COUNT(a) FROM Article a LEFT JOIN a.category c GROUP BY c.name ORDER BY COUNT(a) DESC")
  java.util.List<Object[]> countGroupedByCategory();

  @org.springframework.data.jpa.repository.Query("""
      SELECT function('date', a.createdAt), COUNT(a)
      FROM Article a
      WHERE a.createdAt >= :start AND a.createdAt < :end
      GROUP BY function('date', a.createdAt)
      ORDER BY function('date', a.createdAt)
      """)
  java.util.List<Object[]> countCreatedGroupedByDate(java.time.Instant start, java.time.Instant end);

  @org.springframework.data.jpa.repository.Query("""
      SELECT a.title AS title, a.views AS views, a.likes AS likes
      FROM Article a
      WHERE a.published = true
      ORDER BY a.createdAt DESC
      """)
  java.util.List<ArticleStatsProjection> findRecentPublishedProjected(Pageable pageable);

  java.util.List<Article> findByCreatedAtBetween(java.time.Instant start, java.time.Instant end);
}
