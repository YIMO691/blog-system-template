package com.example.blog.service.impl;

import com.example.blog.config.UploadProperties;
import com.example.blog.service.UploadService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalUploadService implements UploadService {
  private final UploadProperties properties;

  public LocalUploadService(UploadProperties properties) {
    this.properties = properties;
  }

  @Override
  public String storeImage(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("empty_file");
    }
    if (file.getSize() > properties.getMaxSize()) {
      throw new IllegalArgumentException("file_too_large");
    }
    String contentType = file.getContentType();
    if (contentType == null || !properties.getAllowedContentTypes().contains(contentType.toLowerCase())) {
      throw new IllegalArgumentException("invalid_content_type");
    }
    String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
    String ext = "";
    int idx = original.lastIndexOf('.');
    if (idx >= 0) {
      ext = original.substring(idx).toLowerCase();
    }
    if (!properties.getAllowedExtensions().contains(ext)) {
      throw new IllegalArgumentException("invalid_extension");
    }
    String filename = UUID.randomUUID().toString().replace("-", "") + ext;
    Path dir = Paths.get(properties.getDir()).normalize();
    Files.createDirectories(dir);
    Path target = dir.resolve(filename).normalize();
    if (!target.startsWith(dir)) {
      throw new IllegalArgumentException("invalid_path");
    }
    Files.copy(file.getInputStream(), target);
    return filename;
  }

  @Override
  public Resource loadImage(String filename) throws IOException {
    Path dir = Paths.get(properties.getDir()).normalize();
    Path file = dir.resolve(filename).normalize();
    if (!file.startsWith(dir) || !Files.exists(file) || !Files.isReadable(file)) {
      return null;
    }
    return new FileSystemResource(file.toFile());
  }

  @Override
  public MediaType getMediaType(String filename) {
    if (filename == null) {
      return MediaType.APPLICATION_OCTET_STREAM;
    }
    String lower = filename.toLowerCase();
    if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
    if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
    if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
    return MediaType.APPLICATION_OCTET_STREAM;
  }
}
