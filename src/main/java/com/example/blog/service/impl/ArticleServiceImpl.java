package com.example.blog.service.impl;

import com.example.blog.dto.ArticleForm;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleLike;
import com.example.blog.entity.User;
import com.example.blog.exception.ForbiddenException;
import com.example.blog.exception.NotFoundException;
import com.example.blog.repository.ArticleLikeRepository;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.service.ArticleService;
import com.example.blog.service.TaxonomyService;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

  private final ArticleRepository articleRepository;
  private final ArticleLikeRepository articleLikeRepository;
  private final com.example.blog.repository.CommentRepository commentRepository;
  private final com.example.blog.repository.CommentLikeRepository commentLikeRepository;
  private final UserService userService;
  private final TaxonomyService taxonomyService;

  @Override
  @Transactional(readOnly = true)
  public Page<Article> listPublished(int page, int size) {
    Page<Article> articles = articleRepository.findByPublishedTrueOrderByCreatedAtDesc(PageRequest.of(page, size));
    // Force initialization of tags to avoid LazyInitializationException in view
    articles.forEach(a -> a.getTags().size());
    return articles;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Article> searchPublished(String keyword, int page, int size) {
    String k = (keyword == null ? "" : keyword.trim()).toLowerCase(Locale.ROOT);
    org.springframework.data.jpa.domain.Specification<Article> spec = (root, query, cb) -> {
      jakarta.persistence.criteria.Expression<String> title = root.get("title");
      jakarta.persistence.criteria.Expression<String> content = root.get("content");
      jakarta.persistence.criteria.Predicate likeTitle = cb.like(title, "%" + keyword + "%");
      jakarta.persistence.criteria.Predicate likeContent = cb.like(content, "%" + keyword + "%");
      query.orderBy(cb.desc(root.get("createdAt")));
      return cb.and(cb.isTrue(root.get("published")), cb.or(likeTitle, likeContent));
    };
    Page<Article> articles = articleRepository.findAll(spec, PageRequest.of(page, size));
    // Force initialization of tags
    articles.forEach(a -> {
      a.getTags().size();
      if (a.getCategory() != null) { a.getCategory().getName(); }
      a.getAuthor().getUsername();
    });
    return articles;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Article> listPublishedByCategory(Long categoryId, int page, int size) {
    Page<Article> articles = articleRepository.findByPublishedTrueAndCategoryIdOrderByCreatedAtDesc(categoryId, PageRequest.of(page, size));
    articles.forEach(a -> a.getTags().size());
    return articles;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Article> listPublishedByTag(String tagName, int page, int size) {
    Page<Article> articles = articleRepository.findByPublishedTrueAndTagsNameOrderByCreatedAtDesc(tagName, PageRequest.of(page, size));
    articles.forEach(a -> a.getTags().size());
    return articles;
  }

  @Override
  @Transactional(readOnly = true)
  public Article getPublishedBySlug(String slug) {
    Article article = articleRepository.findBySlugAndPublishedTrue(slug)
        .orElseThrow(() -> new NotFoundException("文章不存在或未发布"));
    // Force initialization of tags
    article.getTags().size();
    return article;
  }

  @Override
  @Transactional(readOnly = true)
  public Article getById(Long id) {
    Article article = articleRepository.findById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
    // 强制初始化 tags
    article.getTags().size();
    return article;
  }

  @Override
  @Transactional
  public Article createOrUpdate(Long id, ArticleForm form) {
    User currentUser = userService.getCurrentUserOrThrow();
    if (currentUser.isMuted()) {
      throw new ForbiddenException("您已被禁言，无法发布或编辑文章");
    }
    if (!"admin".equals(currentUser.getUsername())) {
      throw new ForbiddenException("仅管理员可进行文章书写");
    }

    Article a;
    if (id == null) {
      a = Article.builder().author(currentUser).build();
    } else {
      a = articleRepository.findById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
      // 检查权限：只有作者可以修改
      if (!a.getAuthor().getId().equals(currentUser.getId())) {
        throw new ForbiddenException("无权修改此文章");
      }
    }

    a.setTitle(form.title());
    a.setContent(form.content());
    a.setPublished(form.published());

    if (a.getSlug() == null || a.getSlug().isBlank()) {
      a.setSlug(makeSlug(form.title()));
    }

    if (form.newCategory() != null && !form.newCategory().isBlank()) {
        a.setCategory(taxonomyService.createCategory(form.newCategory()));
    } else {
        // If categoryId is null or empty, category will be set to null (Uncategorized)
        a.setCategory(taxonomyService.getCategoryOrNull(form.categoryId()));
    }
    a.getTags().clear();
    a.getTags().addAll(taxonomyService.resolveTags(form.tags()));

    if (id == null) {
      a = articleRepository.save(a);
    } else {
      a = articleRepository.save(a);
    }
    return a;
  }

  @Override
  @Transactional
  public void delete(Long id) {
    Article a = articleRepository.findById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
    User currentUser = userService.getCurrentUserOrThrow();
    
    // 权限检查：只有作者或管理员可以删除
    boolean isAuthor = a.getAuthor().getId().equals(currentUser.getId());
    boolean isAdmin = currentUser.getRole() == com.example.blog.common.Role.ROLE_ADMIN;
    
    if (!isAuthor && !isAdmin) {
      throw new ForbiddenException("无权删除此文章");
    }

    // 1. Delete Article Likes
    articleLikeRepository.deleteByArticleId(id);

    // 2. Delete Comments and their Likes
    java.util.List<com.example.blog.entity.Comment> comments = commentRepository.findByArticleId(id);
    if (!comments.isEmpty()) {
        java.util.List<Long> commentIds = comments.stream()
            .map(com.example.blog.entity.Comment::getId)
            .collect(java.util.stream.Collectors.toList());
        
        commentLikeRepository.deleteByCommentIdIn(commentIds);
        commentRepository.deleteAll(comments);
    }
    
    articleRepository.deleteById(id);
  }

  @Override
  public java.util.List<Article> listDrafts(Long authorId) {
    return articleRepository.findByAuthorIdAndPublishedFalseOrderByCreatedAtDesc(authorId);
  }

  @Override
  @Transactional
  public void increaseViewCount(Long id) {
    Article a = articleRepository.findById(id).orElse(null);
    if (a != null) {
      a.setViews(a.getViews() + 1);
      articleRepository.save(a);
    }
  }

  @Override
  @Transactional
  public void toggleLike(Long id) {
    Article a = articleRepository.findById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
    User currentUser = userService.getCurrentUserOrThrow();

    if (articleLikeRepository.existsByArticleIdAndUserId(id, currentUser.getId())) {
      // Unlike
      articleLikeRepository.deleteByArticleIdAndUserId(id, currentUser.getId());
      a.setLikes(Math.max(0, a.getLikes() - 1));
    } else {
      // Like
      articleLikeRepository.save(ArticleLike.builder().article(a).user(currentUser).build());
      a.setLikes(a.getLikes() + 1);
    }
    articleRepository.save(a);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isLikedByCurrentUser(Long id) {
    try {
      User currentUser = userService.getCurrentUserOrThrow();
      return articleLikeRepository.existsByArticleIdAndUserId(id, currentUser.getId());
    } catch (Exception e) {
      return false;
    }
  }

  private String makeSlug(String title) {
    String base = title == null ? "post" : title;
    String normalized = Normalizer.normalize(base, Normalizer.Form.NFKD)
        .replaceAll("[^\\p{Alnum}]+", "-")
        .replaceAll("(^-|-$)", "")
        .toLowerCase(Locale.ROOT);
    if (normalized.isBlank()) normalized = "post";
    return normalized + "-" + UUID.randomUUID().toString().substring(0, 8);
  }
}
