package com.example.blog.service;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
  String storeImage(MultipartFile file) throws java.io.IOException;
  Resource loadImage(String filename) throws java.io.IOException;
  MediaType getMediaType(String filename);
  String resolveImageUrl(String filename);
}
