package com.example.blog.repository.projection;

public interface ArticleStatsProjection {
  String getTitle();
  Integer getViews();
  Integer getLikes();
}
