package com.example.blog.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownServiceTest {

  private final MarkdownService markdownService = new MarkdownService();

  @Test
  void rendersMarkdownFeaturesToHtml() {
    String html = markdownService.render("## 标题\n\n- 条目\n\n[官网](https://example.com)");

    assertTrue(html.contains("<h2>标题</h2>"));
    assertTrue(html.contains("<li>条目</li>"));
    assertTrue(html.contains("<a href=\"https://example.com\">官网</a>"));
  }

  @Test
  void convertsMarkdownToPlainTextForSummary() {
    String plainText = markdownService.toPlainText("**加粗** 内容\n\n> 引用");

    assertTrue(plainText.contains("加粗"));
    assertTrue(plainText.contains("内容"));
    assertTrue(plainText.contains("引用"));
  }
}
