package com.example.blog.controller;

import com.example.blog.dto.ArticleForm;
import com.example.blog.dto.CommentForm;
import com.example.blog.entity.Article;
import com.example.blog.service.ArticleService;
import com.example.blog.service.CommentService;
import com.example.blog.service.TaxonomyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

  private final ArticleService articleService;
  private final CommentService commentService;
  private final TaxonomyService taxonomyService;
  private final com.example.blog.service.UserService userService;

  @GetMapping
  public String list(@RequestParam(defaultValue = "0") int page, 
                     @RequestParam(required = false) Long category,
                     @RequestParam(required = false) String tag,
                     @RequestParam(defaultValue = "latest") String sort,
                     Model model) {
    org.springframework.data.domain.Page<Article> articles;
    String filterDesc = "";

    if (category != null) {
        if (category == -1) {
            articles = articleService.listPublishedByCategorySorted(null, sort, page, 20);
            filterDesc = "分类筛选";
            model.addAttribute("filterLabel", "未分类");
        } else {
            articles = articleService.listPublishedByCategorySorted(category, sort, page, 20);
            filterDesc = "分类筛选";
            model.addAttribute("filterLabel", "分类ID: " + category);
        }
    } else if (tag != null && !tag.isBlank()) {
        articles = articleService.listPublishedByTagSorted(tag, sort, page, 20);
        filterDesc = "标签筛选";
        model.addAttribute("filterLabel", "标签: " + tag);
    } else {
        articles = articleService.listPublishedSorted(sort, page, 100);
    }
    
    model.addAttribute("page", articles);
    if (model.getAttribute("keyword") == null) {
        model.addAttribute("keyword", "");
    }
    
    // 加载所有分类，用于侧边栏展示
    model.addAttribute("allCategories", taxonomyService.listCategories());
    model.addAttribute("currentCategoryId", category);
    model.addAttribute("currentTag", tag);
    model.addAttribute("sort", sort);

    // Only group if no specific filter is applied (or keep grouping but maybe less relevant)
    // For now, let's keep grouping logic but maybe we only want to group when showing ALL articles.
    // However, user requirement "Modify article list, arrange by category" implies grouping is preferred.
    // If we filter by category, grouping by category results in 1 group.
    
    java.util.Map<String, java.util.List<Article>> grouped = articles.getContent().stream()
        .collect(java.util.stream.Collectors.groupingBy(
            a -> a.getCategory() == null ? "未分类" : a.getCategory().getName(),
            java.util.TreeMap::new,
            java.util.stream.Collectors.toList()
        ));
    model.addAttribute("groupedArticles", grouped);
    
    return "article/list";
  }

  @GetMapping("/search")
  public String search(@RequestParam String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "latest") String sort,
                       Model model) {
    model.addAttribute("keyword", keyword);
    model.addAttribute("sort", sort);
    model.addAttribute("page", articleService.searchPublishedSorted(keyword, sort, page, 10));
    model.addAttribute("allCategories", taxonomyService.listCategories());
    return "article/list";
  }

  @GetMapping("/{slug}")
  public String detail(@PathVariable String slug, 
                       @RequestParam(required = false) boolean liked,
                       Model model) {
    Article a = articleService.getPublishedBySlug(slug);
    
    // 只有非点赞操作的重定向才增加浏览量
    if (!liked) {
        articleService.increaseViewCount(a.getId());
    }
    
    model.addAttribute("article", a);
    model.addAttribute("comments", commentService.listApprovedByArticle(a.getId()));
    model.addAttribute("commentForm", new CommentForm("", null));
    model.addAttribute("likedByCurrentUser", articleService.isLikedByCurrentUser(a.getId()));
    return "article/detail";
  }

  @PostMapping("/{id}/like")
  public String likeArticle(@PathVariable Long id) {
    articleService.toggleLike(id);
    Article a = articleService.getById(id);
    return "redirect:/articles/" + a.getSlug() + "?liked=true";
  }

  @PostMapping("/{slug}/comments")
  public String addComment(@PathVariable String slug,
                           @ModelAttribute("commentForm") @Valid CommentForm form,
                           BindingResult br,
                           Model model) {
    Article a = articleService.getPublishedBySlug(slug);
    if (br.hasErrors()) {
      model.addAttribute("article", a);
      model.addAttribute("comments", commentService.listApprovedByArticle(a.getId()));
      return "article/detail";
    }
    commentService.addComment(a.getId(), form);
    return "redirect:/articles/" + slug + "?commented";
  }

  @PostMapping("/{slug}/comments/{id}/like")
  public String likeComment(@PathVariable String slug, @PathVariable Long id) {
    commentService.likeComment(id);
    return "redirect:/articles/" + slug + "?liked=true#comment-" + id;
  }

  // editor pages (requires login via security)
  @GetMapping("/editor/new")
  public String createForm(Model model) {
    com.example.blog.entity.User currentUser = userService.getCurrentUserOrThrow();
    if (!"admin".equals(currentUser.getUsername())) {
      return "redirect:/?error=no_permission";
    }
    java.util.List<Article> drafts = articleService.listDrafts(currentUser.getId());
    if (!drafts.isEmpty()) {
      model.addAttribute("drafts", drafts);
    }
    model.addAttribute("form", new ArticleForm("", "", false, null, null, Set.of()));
    model.addAttribute("categories", taxonomyService.listCategories());
    model.addAttribute("articleId", null);
    return "article/editor";
  }

  @GetMapping("/editor/{id}")
  public String editForm(@PathVariable Long id, Model model) {
    com.example.blog.entity.User currentUser = userService.getCurrentUserOrThrow();
    if (!"admin".equals(currentUser.getUsername())) {
      return "redirect:/?error=no_permission";
    }
    Article a = articleService.getById(id);
    
    // Convert Set<Tag> to Set<String> tag names
    Set<String> tagNames = a.getTags().stream()
        .map(com.example.blog.entity.Tag::getName)
        .collect(java.util.stream.Collectors.toSet());
    
    model.addAttribute("form", new ArticleForm(
        a.getTitle(), 
        a.getContent(), 
        a.isPublished(), 
        a.getCategory() != null ? a.getCategory().getId() : null, 
        null,
        tagNames
    ));
    model.addAttribute("categories", taxonomyService.listCategories());
    model.addAttribute("articleId", id);
    return "article/editor";
  }

  @PostMapping("/editor")
  public String create(@ModelAttribute("form") @Valid ArticleForm form, 
                       BindingResult br, 
                       @RequestParam(required = false) Long id,
                       Model model) {
    com.example.blog.entity.User currentUser = userService.getCurrentUserOrThrow();
    if (!"admin".equals(currentUser.getUsername())) {
      return "redirect:/?error=no_permission";
    }
    if (br.hasErrors()) {
      model.addAttribute("categories", taxonomyService.listCategories());
      return "article/editor";
    }
    Article saved = articleService.createOrUpdate(id, form);
    return "redirect:/";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id) {
    articleService.delete(id);
    return "redirect:/";
  }

  @PostMapping("/upload-image")
  public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) throws java.io.IOException {
    com.example.blog.entity.User currentUser = userService.getCurrentUserOrThrow();
    if (!"admin".equals(currentUser.getUsername())) {
      return ResponseEntity.status(403).body(Map.of("error", "no_permission"));
    }
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "empty_file"));
    }
    String original = file.getOriginalFilename();
    String ext = "";
    if (original != null && original.contains(".")) {
      ext = original.substring(original.lastIndexOf(".")).toLowerCase();
    }
    String name = UUID.randomUUID().toString().replace("-", "") + ext;
    Path dir = Paths.get(System.getProperty("user.dir"), "uploads");
    if (!Files.exists(dir)) {
      Files.createDirectories(dir);
    }
    Path target = dir.resolve(name);
    Files.write(target, file.getBytes());
    String url = "/articles/image/" + name;
    return ResponseEntity.ok(Map.of("url", url));
  }

  @GetMapping("/image/{filename}")
  public ResponseEntity<Resource> getImage(@PathVariable String filename) throws java.io.IOException {
    Path file = Paths.get(System.getProperty("user.dir"), "uploads", filename);
    if (!Files.exists(file)) {
      return ResponseEntity.notFound().build();
    }
    FileSystemResource resource = new FileSystemResource(file.toFile());
    return ResponseEntity.ok(resource);
  }
}
