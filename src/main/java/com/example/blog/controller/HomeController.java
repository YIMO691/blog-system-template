package com.example.blog.controller;

import com.example.blog.service.ArticleService;
import com.example.blog.service.CommentService;
import com.example.blog.service.TaxonomyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

  private final ArticleService articleService;
  private final CommentService commentService;
  private final TaxonomyService taxonomyService;

  @GetMapping("/")
  public String home(@RequestParam(defaultValue = "0") int page, Model model) {
    org.springframework.data.domain.Page<com.example.blog.entity.Article> latestPage =
        articleService.listPublished(0, 6);
    model.addAttribute("latest", latestPage.getContent());
    model.addAttribute("articleTotal", latestPage.getTotalElements());
    model.addAttribute("recentComments", commentService.listRecent(6));
    model.addAttribute("categories", taxonomyService.listCategories());
    return "index";
  }
}
