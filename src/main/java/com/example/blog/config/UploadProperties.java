package com.example.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {
  private String dir = System.getProperty("user.dir") + "/uploads";
  private long maxSize = 5 * 1024 * 1024;
  private List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
  private List<String> allowedContentTypes = List.of("image/jpeg", "image/png", "image/gif", "image/webp");

  public String getDir() {
    return dir;
  }

  public void setDir(String dir) {
    this.dir = dir;
  }

  public long getMaxSize() {
    return maxSize;
  }

  public void setMaxSize(long maxSize) {
    this.maxSize = maxSize;
  }

  public List<String> getAllowedExtensions() {
    return allowedExtensions;
  }

  public void setAllowedExtensions(List<String> allowedExtensions) {
    this.allowedExtensions = allowedExtensions;
  }

  public List<String> getAllowedContentTypes() {
    return allowedContentTypes;
  }

  public void setAllowedContentTypes(List<String> allowedContentTypes) {
    this.allowedContentTypes = allowedContentTypes;
  }
}
