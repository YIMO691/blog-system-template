package com.example.blog;

import com.example.blog.common.AdminPermission;
import com.example.blog.common.ArticleStatus;
import com.example.blog.common.Role;
import com.example.blog.entity.Article;
import com.example.blog.entity.Category;
import com.example.blog.entity.Tag;
import com.example.blog.entity.User;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.TagRepository;
import com.example.blog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ArticleAuditFlowIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ArticleRepository articleRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TagRepository tagRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  private User normalUser;
  private User admin;

  @BeforeEach
  void setUp() {
    normalUser = userRepository.findByUsername("reader")
        .orElseGet(() -> userRepository.save(User.builder()
            .username("reader")
            .nickname("reader")
            .passwordHash("noop")
            .role(Role.ROLE_USER)
            .createdAt(Instant.now())
            .enabled(true)
            .build()));

    admin = userRepository.findByUsername("admin")
        .orElseGet(() -> userRepository.save(User.builder()
            .username("admin")
            .nickname("admin")
            .passwordHash("noop")
            .role(Role.ROLE_ADMIN)
            .createdAt(Instant.now())
            .enabled(true)
            .build()));
    admin.setRole(Role.ROLE_ADMIN);
    admin.setSuperAdmin(true);
    admin.grantAdminPermissions(AdminPermission.superAdminPermissions());
    admin = userRepository.save(admin);
  }

  @Test
  void normalUser_shouldNotAccessEditorPage() throws Exception {
    mockMvc.perform(get("/articles/editor/new")
            .with(SecurityMockMvcRequestPostProcessors.user("reader")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCanCreateDraftArticle() throws Exception {
    String title = "draft-" + UUID.randomUUID();

    mockMvc.perform(post("/articles/editor")
            .with(csrf())
            .with(SecurityMockMvcRequestPostProcessors.user("admin")
                .authorities(new SimpleGrantedAuthority("ADMIN_ARTICLE_WRITE")))
            .param("title", title)
            .param("content", "draft content")
            .param("status", "DRAFT")
            .param("tags", "java,spring"))
        .andExpect(status().is3xxRedirection());

    Article saved = articleRepository.findAll().stream()
        .filter(article -> title.equals(article.getTitle()))
        .findFirst()
        .orElseThrow();
    assertThat(saved.getStatus()).isEqualTo(ArticleStatus.DRAFT);
    assertThat(saved.isPublished()).isFalse();
  }

  @Test
  void publishedArticle_shouldBeVisibleOnFrontend() throws Exception {
    String title = "published-" + UUID.randomUUID();
    Article article = articleRepository.save(Article.builder()
        .title(title)
        .slug("published-" + UUID.randomUUID())
        .content("content")
        .author(admin)
        .status(ArticleStatus.PUBLISHED)
        .build());

    mockMvc.perform(get("/articles/" + article.getSlug()))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString(title)));
  }

  @Test
  void draftAndOfflineArticles_shouldNotBeVisibleOnFrontend() throws Exception {
    Article draft = articleRepository.save(Article.builder()
        .title("draft-" + UUID.randomUUID())
        .slug("draft-" + UUID.randomUUID())
        .content("content")
        .author(admin)
        .status(ArticleStatus.DRAFT)
        .build());
    Article offline = articleRepository.save(Article.builder()
        .title("offline-" + UUID.randomUUID())
        .slug("offline-" + UUID.randomUUID())
        .content("content")
        .author(admin)
        .status(ArticleStatus.OFFLINE)
        .build());

    mockMvc.perform(get("/articles/" + draft.getSlug()))
        .andExpect(status().isNotFound());
    mockMvc.perform(get("/articles/" + offline.getSlug()))
        .andExpect(status().isNotFound());
  }

  @Test
  void publishEndpoint_shouldMakeDraftVisible() throws Exception {
    Article article = articleRepository.save(Article.builder()
        .title("publish-" + UUID.randomUUID())
        .slug("publish-" + UUID.randomUUID())
        .content("content")
        .author(admin)
        .status(ArticleStatus.DRAFT)
        .build());

    mockMvc.perform(post("/admin/articles/{id}/publish", article.getId())
            .with(csrf())
            .with(SecurityMockMvcRequestPostProcessors.user("admin")
                .authorities(new SimpleGrantedAuthority("ADMIN_ARTICLE_MANAGE")))
            .header("X-Requested-With", "XMLHttpRequest"))
        .andExpect(status().isOk());

    Article updated = articleRepository.findById(article.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
  }

  @Test
  void search_shouldOnlyShowPublishedArticles() throws Exception {
    String keyword = "searchable-" + UUID.randomUUID();
    articleRepository.saveAll(java.util.List.of(
        Article.builder()
            .title("published-" + keyword)
            .slug("published-" + UUID.randomUUID())
            .content(keyword)
            .author(admin)
            .status(ArticleStatus.PUBLISHED)
            .build(),
        Article.builder()
            .title("draft-" + keyword)
            .slug("draft-" + UUID.randomUUID())
            .content(keyword)
            .author(admin)
            .status(ArticleStatus.DRAFT)
            .build(),
        Article.builder()
            .title("offline-" + keyword)
            .slug("offline-" + UUID.randomUUID())
            .content(keyword)
            .author(admin)
            .status(ArticleStatus.OFFLINE)
            .build()
    ));

    mockMvc.perform(get("/articles/search").param("keyword", keyword))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("published-")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString(keyword)))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("<mark class=\"search-highlight\">")))
        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("draft-" + keyword))))
        .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("offline-" + keyword))));
  }

  @Test
  void search_shouldMatchArticleTags() throws Exception {
    String tagKeyword = "TagSearch-" + UUID.randomUUID();
    Tag tag = tagRepository.save(Tag.builder().name(tagKeyword.toLowerCase()).build());
    Article taggedArticle = Article.builder()
        .title("only-tag-match-" + UUID.randomUUID())
        .slug("only-tag-match-" + UUID.randomUUID())
        .content("this article body does not include the search keyword")
        .author(admin)
        .status(ArticleStatus.PUBLISHED)
        .build();
    taggedArticle.getTags().add(tag);
    articleRepository.save(taggedArticle);

    mockMvc.perform(get("/articles/search").param("keyword", tagKeyword))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("命中：")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("标签")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString(taggedArticle.getTitle())));
  }

  @Test
  void search_shouldMatchArticleCategory() throws Exception {
    String categoryKeyword = "GlobalCategory-" + UUID.randomUUID();
    Category category = categoryRepository.save(Category.builder()
        .name(categoryKeyword)
        .description("category for global search")
        .build());
    Article categorizedArticle = articleRepository.save(Article.builder()
        .title("only-category-match-" + UUID.randomUUID())
        .slug("only-category-match-" + UUID.randomUUID())
        .content("body without category keyword")
        .author(admin)
        .category(category)
        .status(ArticleStatus.PUBLISHED)
        .build());

    mockMvc.perform(get("/articles/search").param("keyword", categoryKeyword.toLowerCase()))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("分类")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString(categorizedArticle.getTitle())));
  }

  @Test
  void search_shouldMatchArticleAuthorInfo() throws Exception {
    String authorKeyword = ("author-" + UUID.randomUUID()).substring(0, 20);
    User author = userRepository.save(User.builder()
        .username(authorKeyword)
        .nickname("nick-" + authorKeyword)
        .displayName("show-" + authorKeyword)
        .passwordHash("noop")
        .role(Role.ROLE_ADMIN)
        .createdAt(Instant.now())
        .enabled(true)
        .build());
    Article authorMatchedArticle = articleRepository.save(Article.builder()
        .title("only-author-match-" + UUID.randomUUID())
        .slug("only-author-match-" + UUID.randomUUID())
        .content("body without author keyword")
        .author(author)
        .status(ArticleStatus.PUBLISHED)
        .build());

    mockMvc.perform(get("/articles/search").param("keyword", authorKeyword))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("作者")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString(authorMatchedArticle.getTitle())));
  }

  @Test
  void offlineEndpoint_shouldHidePublishedArticle() throws Exception {
    Article article = articleRepository.save(Article.builder()
        .title("offline-target-" + UUID.randomUUID())
        .slug("offline-target-" + UUID.randomUUID())
        .content("content")
        .author(admin)
        .status(ArticleStatus.PUBLISHED)
        .build());

    mockMvc.perform(post("/admin/articles/{id}/offline", article.getId())
            .with(csrf())
            .with(SecurityMockMvcRequestPostProcessors.user("admin")
                .authorities(new SimpleGrantedAuthority("ADMIN_ARTICLE_MANAGE")))
            .header("X-Requested-With", "XMLHttpRequest"))
        .andExpect(status().isOk());

    Article updated = articleRepository.findById(article.getId()).orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(ArticleStatus.OFFLINE);
  }
}
