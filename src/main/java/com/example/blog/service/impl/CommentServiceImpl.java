package com.example.blog.service.impl;

import com.example.blog.dto.CommentDto;
import com.example.blog.dto.CommentForm;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.CommentLike;
import com.example.blog.entity.User;
import com.example.blog.exception.NotFoundException;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.repository.CommentLikeRepository;
import com.example.blog.repository.CommentRepository;
import com.example.blog.service.CommentService;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final CommentLikeRepository commentLikeRepository;
  private final ArticleRepository articleRepository;
  private final UserService userService;
  private final com.example.blog.service.NotificationService notificationService;

  @Override
  @Transactional(readOnly = true)
  public List<CommentDto> listApprovedByArticle(Long articleId) {
    List<Comment> comments = commentRepository.findByArticleIdAndApprovedTrueOrderByCreatedAtAsc(articleId);

    // Get current user ID if logged in
    Long currentUserId = null;
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
      try {
        currentUserId = userService.getCurrentUserOrThrow().getId();
      } catch (Exception ignored) {}
    }

    Set<Long> likedCommentIds = new HashSet<>();
    if (currentUserId != null && !comments.isEmpty()) {
      List<Long> commentIds = comments.stream()
          .map(Comment::getId)
          .toList();
      commentLikeRepository.findByUserIdAndCommentIdIn(currentUserId, commentIds).stream()
          .map(like -> like.getComment().getId())
          .forEach(likedCommentIds::add);
    }

    // Map ID -> DTO
    Map<Long, CommentDto> dtoMap = new HashMap<>();
    for (Comment c : comments) {
      dtoMap.put(c.getId(), new CommentDto(
          c.getId(),
          c.getDisplayName(),
          c.getContent(),
          c.getCreatedAt(),
          c.getLikes(),
          likedCommentIds.contains(c.getId()),
          new ArrayList<>()
      ));
    }

    // Build tree
    List<CommentDto> roots = new ArrayList<>();
    for (Comment c : comments) {
      CommentDto currentDto = dtoMap.get(c.getId());
      if (c.getParent() == null) {
        roots.add(currentDto);
      } else {
        // Parent must also be approved (in the list)
        CommentDto parentDto = dtoMap.get(c.getParent().getId());
        if (parentDto != null) {
          parentDto.replies().add(currentDto);
        }
      }
    }
    return roots;
  }

  @Override
  @Transactional
  public Comment addComment(Long articleId, CommentForm form) {
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new NotFoundException("文章不存在"));

    User current = userService.getCurrentUserOrThrow();
    if (current.isMuted()) {
      throw new com.example.blog.exception.ForbiddenException("您已被禁言，无法发表评论");
    }

    String name = (current.getNickname() != null && !current.getNickname().isBlank())
        ? current.getNickname()
        : "用户";
    String finalDisplayName = String.format("%s (ID: %d)", name, current.getId());

    Comment parent = null;
    if (form.parentId() != null) {
      parent = commentRepository.findById(form.parentId())
          .orElseThrow(() -> new NotFoundException("父评论不存在"));
      // 可选：检查 parent.getArticle().getId().equals(articleId)
    }

    Comment c = Comment.builder()
        .article(article)
        .user(current)
        .displayName(finalDisplayName)
        .content(form.content())
        .approved(current.canModerateComments())
        .parent(parent)
        .build();

    Comment saved = commentRepository.save(c);
    if (!current.canModerateComments()) {
      notificationService.notifyAdmin(
          com.example.blog.common.NotificationType.COMMENT_PENDING,
          "有新的评论待审核",
          "/admin/comments"
      );
    }
    return saved;
  }

  @Override
  public List<Comment> listPending() {
    return commentRepository.findByApprovedFalseOrderByCreatedAtDesc();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Comment> listRecent(int limit) {
    List<Comment> comments = commentRepository.findByApprovedTrueOrderByCreatedAtDesc(
        org.springframework.data.domain.PageRequest.of(0, limit)
    ).getContent();
    // Initialize article to avoid lazy loading issues in view
    comments.forEach(c -> c.getArticle().getTitle());
    return comments;
  }

  @Override
  @Transactional
  public void approve(Long commentId) {
    Comment c = commentRepository.findById(commentId)
        .orElseThrow(() -> new NotFoundException("评论不存在"));
    c.setApproved(true);
    commentRepository.save(c);
    if (c.getUser() != null) {
      notificationService.notifyUser(
          c.getUser().getId(),
          com.example.blog.common.NotificationType.COMMENT_APPROVED,
          "你的评论已通过审核",
          "/articles/" + c.getArticle().getSlug() + "#comment-" + c.getId()
      );
    }
  }

  @Override
  @Transactional
  public void delete(Long commentId) {
    Comment c = commentRepository.findById(commentId)
        .orElseThrow(() -> new NotFoundException("评论不存在"));

    Long userId = c.getUser() != null ? c.getUser().getId() : null;
    String articleSlug = c.getArticle().getSlug();
    List<Long> commentIdsToDelete = new ArrayList<>();
    collectCommentTreeIds(c, commentIdsToDelete);

    commentLikeRepository.deleteByCommentIdIn(commentIdsToDelete);
    commentRepository.delete(c);

    if (userId != null) {
      notificationService.notifyUser(
          userId,
          com.example.blog.common.NotificationType.COMMENT_DELETED,
          "你的评论已被删除",
          "/articles/" + articleSlug
      );
    }
  }

  @Override
  @Transactional
  public CommentService.LikeResult likeComment(Long commentId) {
    Comment c = commentRepository.findById(commentId)
        .orElseThrow(() -> new NotFoundException("评论不存在"));
    
    User current = userService.getCurrentUserOrThrow();
    
    // Check if already liked
    if (commentLikeRepository.existsByCommentIdAndUserId(commentId, current.getId())) {
      // Toggle: Unlike
      commentLikeRepository.deleteByCommentIdAndUserId(commentId, current.getId());
      c.setLikes(Math.max(0, c.getLikes() - 1));
      commentRepository.save(c);
      return new CommentService.LikeResult(false, c.getLikes());
    }

    // Record like
    CommentLike like = CommentLike.builder()
        .comment(c)
        .user(current)
        .build();
    commentLikeRepository.save(like);

    c.setLikes(c.getLikes() + 1);
    commentRepository.save(c);
    return new CommentService.LikeResult(true, c.getLikes());
  }

  private void collectCommentTreeIds(Comment comment, List<Long> ids) {
    ids.add(comment.getId());
    for (Comment reply : comment.getReplies()) {
      collectCommentTreeIds(reply, ids);
    }
  }
}
