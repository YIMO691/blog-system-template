package com.example.blog.repository;

import com.example.blog.common.ArticleStatus;
import com.example.blog.entity.Article;
import com.example.blog.repository.projection.ArticleStatsProjection;
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
  Page<Article> findByStatusOrderByCreatedAtDesc(ArticleStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {"category", "author"})
  Optional<Article> findBySlugAndStatus(String slug, ArticleStatus status);

  // basic search
  @EntityGraph(attributePaths = {"category", "author"})
  Page<Article> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(ArticleStatus status, String keyword, Pageable pageable);

  // Specification + EntityGraph 支持（用于标题或正文内容联合搜索）
  @EntityGraph(attributePaths = {"category", "author"})
  Page<Article> findAll(Specification<Article> spec, Pageable pageable);

  @EntityGraph(attributePaths = {"category", "author"})
  Page<Article> findByStatusAndCategoryIdOrderByCreatedAtDesc(ArticleStatus status, Long categoryId, Pageable pageable);

  @EntityGraph(attributePaths = {"category", "author"})
  Page<Article> findByStatusAndTagsNameOrderByCreatedAtDesc(ArticleStatus status, String tagName, Pageable pageable);

  @EntityGraph(attributePaths = {"category", "author"})
  java.util.List<Article> findByAuthorIdAndStatusInOrderByUpdatedAtDesc(Long authorId, java.util.Collection<ArticleStatus> statuses);

  long countByStatus(ArticleStatus status);

  @org.springframework.data.jpa.repository.Query("SELECT SUM(a.views) FROM Article a")
  Long sumViews();

  @org.springframework.data.jpa.repository.Query("SELECT SUM(a.likes) FROM Article a")
  Long sumLikes();

  @org.springframework.data.jpa.repository.Query("""
      SELECT a.title AS title, a.views AS views, a.likes AS likes
      FROM Article a
      WHERE a.status = :status
      ORDER BY a.views DESC, a.createdAt DESC
      """)
  java.util.List<ArticleStatsProjection> findTopProjectedByStatusOrderByViewsDesc(@Param("status") ArticleStatus status, Pageable pageable);

  @org.springframework.data.jpa.repository.Query("""
      SELECT a.title AS title, a.views AS views, a.likes AS likes
      FROM Article a
      WHERE a.status = :status
      ORDER BY a.likes DESC, a.createdAt DESC
      """)
  java.util.List<ArticleStatsProjection> findTopProjectedByStatusOrderByLikesDesc(@Param("status") ArticleStatus status, Pageable pageable);

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
      WHERE a.status = :status
      ORDER BY a.createdAt DESC
      """)
  java.util.List<ArticleStatsProjection> findRecentProjectedByStatus(@Param("status") ArticleStatus status, Pageable pageable);

  java.util.List<Article> findByCreatedAtBetween(java.time.Instant start, java.time.Instant end);
}
