package com.example.blog.service.impl;

import com.example.blog.config.UploadProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocalUploadServiceTest {

  @TempDir
  Path tempDir;

  private LocalUploadService uploadService;

  @BeforeEach
  void setUp() {
    UploadProperties props = new UploadProperties();
    props.setDir(tempDir.toString());
    props.setMaxSize(5 * 1024 * 1024);
    props.setAllowedExtensions(List.of(".jpg", ".jpeg", ".png", ".gif", ".webp"));
    props.setAllowedContentTypes(List.of("image/jpeg", "image/png", "image/gif", "image/webp"));
    uploadService = new LocalUploadService(props);
  }

  @Test
  void storeImage_shouldSavePng() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "test.png",
        "image/png",
        "fake-image".getBytes()
    );
    String filename = uploadService.storeImage(file);
    assertNotNull(filename);
    assertTrue(filename.endsWith(".png"));
    Resource resource = uploadService.loadImage(filename);
    assertNotNull(resource);
    assertTrue(resource.exists());
  }

  @Test
  void storeImage_shouldRejectEmptyFile() {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "empty.png",
        "image/png",
        new byte[0]
    );
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> uploadService.storeImage(file)
    );
    assertEquals("empty_file", ex.getMessage());
  }

  @Test
  void storeImage_shouldRejectInvalidExtension() {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "evil.exe",
        "image/png",
        "abc".getBytes()
    );
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> uploadService.storeImage(file)
    );
    assertEquals("invalid_extension", ex.getMessage());
  }

  @Test
  void storeImage_shouldRejectInvalidContentType() {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "test.png",
        "application/octet-stream",
        "abc".getBytes()
    );
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> uploadService.storeImage(file)
    );
    assertEquals("invalid_content_type", ex.getMessage());
  }

  @Test
  void storeImage_shouldRejectTooLargeFile() {
    byte[] content = new byte[6 * 1024 * 1024];
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "big.png",
        "image/png",
        content
    );
    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> uploadService.storeImage(file)
    );
    assertEquals("file_too_large", ex.getMessage());
  }

  @Test
  void loadImage_shouldReturnNullWhenMissing() throws Exception {
    Resource resource = uploadService.loadImage("not-exists.png");
    assertNull(resource);
  }

  @Test
  void resolveImageUrl_shouldUseLocalRouteWhenBaseUrlMissing() {
    assertEquals("/articles/image/demo.png", uploadService.resolveImageUrl("demo.png"));
  }

  @Test
  void resolveImageUrl_shouldUseConfiguredPublicBaseUrl() {
    UploadProperties props = new UploadProperties();
    props.setDir(tempDir.toString());
    props.setPublicBaseUrl("https://cdn.example.com/blog-images");
    props.setAllowedExtensions(List.of(".jpg", ".jpeg", ".png", ".gif", ".webp"));
    props.setAllowedContentTypes(List.of("image/jpeg", "image/png", "image/gif", "image/webp"));
    LocalUploadService externalizedUploadService = new LocalUploadService(props);

    assertEquals("https://cdn.example.com/blog-images/demo.png", externalizedUploadService.resolveImageUrl("demo.png"));
  }
}
