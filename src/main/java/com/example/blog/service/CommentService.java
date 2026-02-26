package com.example.blog.service;

import com.example.blog.dto.CommentDto;
import com.example.blog.dto.CommentForm;
import com.example.blog.entity.Comment;

import java.util.List;

public interface CommentService {
  List<CommentDto> listApprovedByArticle(Long articleId);
  Comment addComment(Long articleId, CommentForm form);
  List<Comment> listPending();
  List<Comment> listRecent(int limit);
  void approve(Long commentId);
  void delete(Long commentId);
  void likeComment(Long commentId);
}
