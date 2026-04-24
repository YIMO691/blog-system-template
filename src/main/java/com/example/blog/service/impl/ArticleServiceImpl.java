package com.example.blog.service.impl;

import com.example.blog.common.ArticleStatus;
import com.example.blog.dto.ArticleForm;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleLike;
import com.example.blog.entity.User;
import com.example.blog.exception.ForbiddenException;
import com.example.blog.exception.NotFoundException;
import com.example.blog.repository.ArticleLikeRepository;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.service.ArticleService;
import com.example.blog.service.MarkdownService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

  private final ArticleRepository articleRepository;
  private final ArticleLikeRepository articleLikeRepository;
  private final com.example.blog.repository.CommentRepository commentRepository;
  private final com.example.blog.repository.CommentLikeRepository commentLikeRepository;
  private final UserService userService;
  private final TaxonomyService taxonomyService;
  private final MarkdownService markdownService;

  @Override
  @Transactional(readOnly = true)
  public Page<Article> listPublished(int page, int size) {
    Page<Article> articles = articleRepository.findByStatusOrderByCreatedAtDesc(ArticleStatus.PUBLISHED, PageRequest.of(page, size));
    // Force initialization of tags to avoid LazyInitializationException in view
    articles.forEach(a -> {
      a.getTags().size();
      a.setSummary(makeSummary(a.getContent(), 150));
    });
    return articles;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Article> searchPublished(String keyword, int page, int size) {
    org.springframework.data.jpa.domain.Specification<Article> spec = buildPublishedSearchSpecification(keyword);
    Page<Article> articles = articleRepository.findAll(spec, PageRequest.of(page, size));
    articles.forEach(a -> {
      a.getTags().size();
      if (a.getCategory() != null) { a.getCategory().getName(); }
      a.getAuthor().getUsername();
      a.setSummary(makeSummary(a.getContent(), 150));
      enrichSearchPresentation(a, keyword);
    });
    return articles;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Article> searchPublishedSorted(String keyword, String sort, int page, int size) {
    org.springframework.data.jpa.domain.Specification<Article> spec = buildPublishedSearchSpecification(keyword);
    org.springframework.data.domain.Page<Article> articles =
        articleRepository.findAll(spec, PageRequest.of(page, size, toSort(sort)));
    articles.forEach(a -> {
      a.getTags().size();
      if (a.getCategory() != null) { a.getCategory().getName(); }
      a.getAuthor().getUsername();
      a.setSummary(makeSummary(a.getContent(), 150));
      enrichSearchPresentation(a, keyword);
    });
    return articles;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Article> listPublishedSorted(String sort, int page, int size) {
    org.springframework.data.domain.Sort s = toSort(sort);
    org.springframework.data.jpa.domain.Specification<Article> spec = (root, query, cb) -> cb.equal(root.get("status"), ArticleStatus.PUBLISHED);
    Page<Article> articles = articleRepository.findAll(spec, PageRequest.of(page, size, s));
    articles.forEach(a -> {
      a.getTags().size();
      a.setSummary(makeSummary(a.getContent(), 150));
    });
    return articles;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Article> listPublishedByCategorySorted(Long categoryId, String sort, int page, int size) {
    org.springframework.data.domain.Sort s = toSort(sort);
    org.springframework.data.jpa.domain.Specification<Article> spec = (root, query, cb) -> {
      if (categoryId == null) {
        return cb.and(cb.equal(root.get("status"), ArticleStatus.PUBLISHED), cb.isNull(root.get("category")));
      } else {
        return cb.and(cb.equal(root.get("status"), ArticleStatus.PUBLISHED), cb.equal(root.get("category").get("id"), categoryId));
      }
    };
    Page<Article> articles = articleRepository.findAll(spec, PageRequest.of(page, size, s));
    articles.forEach(a -> {
      a.getTags().size();
      a.setSummary(makeSummary(a.getContent(), 150));
    });
    return articles;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Article> listPublishedByTagSorted(String tagName, String sort, int page, int size) {
    org.springframework.data.domain.Sort s = toSort(sort);
    org.springframework.data.jpa.domain.Specification<Article> spec = (root, query, cb) -> {
      jakarta.persistence.criteria.Join<com.example.blog.entity.Article, com.example.blog.entity.Tag> tagJoin = root.join("tags", jakarta.persistence.criteria.JoinType.INNER);
      query.distinct(true);
      return cb.and(cb.equal(root.get("status"), ArticleStatus.PUBLISHED), cb.equal(tagJoin.get("name"), tagName));
    };
    Page<Article> articles = articleRepository.findAll(spec, PageRequest.of(page, size, s));
    articles.forEach(a -> {
      a.getTags().size();
      a.setSummary(makeSummary(a.getContent(), 150));
    });
    return articles;
  }

  private org.springframework.data.domain.Sort toSort(String sort) {
    if (sort == null) return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
    String s = sort.trim().toLowerCase(Locale.ROOT);
    if ("hot".equals(s)) return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "likes");
    if ("views".equals(s) || "read".equals(s)) return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "views");
    return org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
  }

  private org.springframework.data.jpa.domain.Specification<Article> buildPublishedSearchSpecification(String keyword) {
    String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
    String pattern = "%" + normalizedKeyword + "%";
    String rawPattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
    return (root, query, cb) -> {
      jakarta.persistence.criteria.Expression<String> title = cb.lower(root.get("title"));
      jakarta.persistence.criteria.Expression<String> content = root.get("content");
      jakarta.persistence.criteria.Join<com.example.blog.entity.Article, com.example.blog.entity.Category> categoryJoin =
          root.join("category", jakarta.persistence.criteria.JoinType.LEFT);
      jakarta.persistence.criteria.Join<com.example.blog.entity.Article, com.example.blog.entity.Tag> tagJoin =
          root.join("tags", jakarta.persistence.criteria.JoinType.LEFT);
      jakarta.persistence.criteria.Join<com.example.blog.entity.Article, com.example.blog.entity.User> authorJoin =
          root.join("author", jakarta.persistence.criteria.JoinType.LEFT);
      jakarta.persistence.criteria.Expression<String> categoryName = cb.lower(categoryJoin.get("name"));
      jakarta.persistence.criteria.Expression<String> categoryDescription = cb.lower(categoryJoin.get("description"));
      jakarta.persistence.criteria.Expression<String> tagName = cb.lower(tagJoin.get("name"));
      jakarta.persistence.criteria.Expression<String> authorUsername = cb.lower(authorJoin.get("username"));
      jakarta.persistence.criteria.Expression<String> authorNickname = cb.lower(authorJoin.get("nickname"));
      jakarta.persistence.criteria.Expression<String> authorDisplayName = cb.lower(authorJoin.get("displayName"));
      jakarta.persistence.criteria.Predicate likeTitle = cb.like(title, pattern);
      // LONGTEXT/CLOB 字段在部分数据库方言下不支持直接套 lower()，这里保留原字段匹配以兼容搜索。
      jakarta.persistence.criteria.Predicate likeContent = cb.like(content, rawPattern);
      jakarta.persistence.criteria.Predicate likeCategoryName = cb.like(categoryName, pattern);
      jakarta.persistence.criteria.Predicate likeCategoryDescription = cb.like(categoryDescription, pattern);
      jakarta.persistence.criteria.Predicate likeTag = cb.like(tagName, pattern);
      jakarta.persistence.criteria.Predicate likeAuthorUsername = cb.like(authorUsername, pattern);
      jakarta.persistence.criteria.Predicate likeAuthorNickname = cb.like(authorNickname, pattern);
      jakarta.persistence.criteria.Predicate likeAuthorDisplayName = cb.like(authorDisplayName, pattern);
      query.distinct(true);
      return cb.and(
          cb.equal(root.get("status"), ArticleStatus.PUBLISHED),
          cb.or(
              likeTitle,
              likeContent,
              likeCategoryName,
              likeCategoryDescription,
              likeTag,
              likeAuthorUsername,
              likeAuthorNickname,
              likeAuthorDisplayName
          )
      );
    };
  }
  @Override
  @Transactional(readOnly = true)
  public Page<Article> listPublishedByCategory(Long categoryId, int page, int size) {
    Page<Article> articles = articleRepository.findByStatusAndCategoryIdOrderByCreatedAtDesc(ArticleStatus.PUBLISHED, categoryId, PageRequest.of(page, size));
    articles.forEach(a -> {
      a.getTags().size();
      a.setSummary(makeSummary(a.getContent(), 150));
    });
    return articles;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Article> listPublishedByTag(String tagName, int page, int size) {
    Page<Article> articles = articleRepository.findByStatusAndTagsNameOrderByCreatedAtDesc(ArticleStatus.PUBLISHED, tagName, PageRequest.of(page, size));
    articles.forEach(a -> {
      a.getTags().size();
      a.setSummary(makeSummary(a.getContent(), 150));
    });
    return articles;
  }

  @Override
  @Transactional(readOnly = true)
  public Article getPublishedBySlug(String slug) {
    Article article = articleRepository.findBySlugAndStatus(slug, ArticleStatus.PUBLISHED)
        .orElseThrow(() -> new NotFoundException("文章不存在或未发布"));
    // Force initialization of tags
    article.getTags().size();
    article.setSummary(makeSummary(article.getContent(), 150));
    return article;
  }

  @Override
  @Transactional(readOnly = true)
  public Article getById(Long id) {
    Article article = articleRepository.findById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
    // 强制初始化 tags
    article.getTags().size();
    article.setSummary(makeSummary(article.getContent(), 150));
    return article;
  }

  @Override
  @Transactional
  public Article createOrUpdate(Long id, ArticleForm form) {
    User currentUser = userService.getCurrentUserOrThrow();
    if (currentUser.isMuted()) {
      throw new ForbiddenException("您已被禁言，无法发布或编辑文章");
    }
    if (!currentUser.canWriteArticles()) {
      throw new ForbiddenException("仅管理员可进行文章书写");
    }

    Article a;
    if (id == null) {
      a = Article.builder().author(currentUser).build();
    } else {
      a = articleRepository.findById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
      // 检查权限：只有作者可以修改
      if (!a.getAuthor().getId().equals(currentUser.getId()) && !currentUser.canManageArticles()) {
        throw new ForbiddenException("无权修改此文章");
      }
    }

    a.setTitle(form.title());
    a.setContent(form.content());
    a.setStatus(resolveEditorStatus(form.status()));

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
    java.util.Set<String> limitedTags = (form.tags() == null) ? java.util.Collections.emptySet()
        : form.tags().stream()
            .filter(java.util.Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .limit(6)
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    a.getTags().addAll(taxonomyService.resolveTags(limitedTags));

    return articleRepository.save(a);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    Article a = articleRepository.findById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
    User currentUser = userService.getCurrentUserOrThrow();
    
    // 权限检查：只有作者或管理员可以删除
    boolean isAuthor = a.getAuthor().getId().equals(currentUser.getId());
    boolean isAdmin = currentUser.canManageArticles();
    
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
    return articleRepository.findByAuthorIdAndStatusInOrderByUpdatedAtDesc(
        authorId,
        java.util.List.of(ArticleStatus.DRAFT, ArticleStatus.OFFLINE)
    );
  }

  @Override
  @Transactional
  public Article publish(Long id) {
    Article article = articleRepository.findById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
    article.setStatus(ArticleStatus.PUBLISHED);
    return articleRepository.save(article);
  }

  @Override
  @Transactional
  public Article offline(Long id) {
    Article article = articleRepository.findById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
    article.setStatus(ArticleStatus.OFFLINE);
    return articleRepository.save(article);
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
  public ArticleService.LikeResult toggleLike(Long id) {
    Article a = articleRepository.findById(id).orElseThrow(() -> new NotFoundException("文章不存在"));
    User currentUser = userService.getCurrentUserOrThrow();

    if (articleLikeRepository.existsByArticleIdAndUserId(id, currentUser.getId())) {
      // Unlike
      articleLikeRepository.deleteByArticleIdAndUserId(id, currentUser.getId());
      a.setLikes(Math.max(0, a.getLikes() - 1));
      articleRepository.save(a);
      return new ArticleService.LikeResult(false, a.getLikes());
    } else {
      // Like
      articleLikeRepository.save(ArticleLike.builder().article(a).user(currentUser).build());
      a.setLikes(a.getLikes() + 1);
      articleRepository.save(a);
      return new ArticleService.LikeResult(true, a.getLikes());
    }
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

  private String makeSummary(String content, int length) {
    if (content == null) return "";
    String plain = markdownService.toPlainText(content);
    plain = plain.replaceAll("\\s+", " ").trim();
    if (plain.length() <= length) return plain;
    return plain.substring(0, length) + "…";
  }

  private void enrichSearchPresentation(Article article, String keyword) {
    if (article == null) {
      return;
    }
    String safeKeyword = keyword == null ? "" : keyword.trim();
    article.setSearchMatchSources(new java.util.LinkedHashSet<>());
    article.setHighlightedTitle(highlightKeyword(article.getTitle(), safeKeyword));
    article.setHighlightedSummary(highlightKeyword(article.getSummary(), safeKeyword));
    if (safeKeyword.isBlank()) {
      return;
    }

    if (containsIgnoreCase(article.getTitle(), safeKeyword)) {
      article.getSearchMatchSources().add("标题");
    }

    String plainContent = markdownService.toPlainText(article.getContent());
    if (containsIgnoreCase(plainContent, safeKeyword)) {
      article.getSearchMatchSources().add("正文");
    }

    if (article.getCategory() != null) {
      if (containsIgnoreCase(article.getCategory().getName(), safeKeyword)
          || containsIgnoreCase(article.getCategory().getDescription(), safeKeyword)) {
        article.getSearchMatchSources().add("分类");
      }
    }

    boolean tagMatched = article.getTags().stream()
        .map(com.example.blog.entity.Tag::getName)
        .anyMatch(tagName -> containsIgnoreCase(tagName, safeKeyword));
    if (tagMatched) {
      article.getSearchMatchSources().add("标签");
    }

    if (article.getAuthor() != null) {
      if (containsIgnoreCase(article.getAuthor().getUsername(), safeKeyword)
          || containsIgnoreCase(article.getAuthor().getNickname(), safeKeyword)
          || containsIgnoreCase(article.getAuthor().getDisplayName(), safeKeyword)) {
        article.getSearchMatchSources().add("作者");
      }
    }
  }

  private boolean containsIgnoreCase(String text, String keyword) {
    if (text == null || keyword == null || keyword.isBlank()) {
      return false;
    }
    return text.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
  }

  private String highlightKeyword(String text, String keyword) {
    String sourceText = text == null ? "" : text;
    String safeText = escapeHtml(sourceText);
    if (keyword == null || keyword.isBlank()) {
      return safeText;
    }

    Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    Matcher matcher = pattern.matcher(sourceText);
    StringBuilder html = new StringBuilder();
    int lastEnd = 0;
    while (matcher.find()) {
      html.append(escapeHtml(sourceText.substring(lastEnd, matcher.start())));
      html.append("<mark class=\"search-highlight\">")
          .append(escapeHtml(matcher.group()))
          .append("</mark>");
      lastEnd = matcher.end();
    }
    if (lastEnd == 0) {
      return safeText;
    }
    html.append(escapeHtml(sourceText.substring(lastEnd)));
    return html.toString();
  }

  private String escapeHtml(String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  private ArticleStatus resolveEditorStatus(ArticleStatus requestedStatus) {
    ArticleStatus safeStatus = requestedStatus == null ? ArticleStatus.DRAFT : requestedStatus;
    return safeStatus == ArticleStatus.PUBLISHED ? ArticleStatus.PUBLISHED : ArticleStatus.DRAFT;
  }
}
