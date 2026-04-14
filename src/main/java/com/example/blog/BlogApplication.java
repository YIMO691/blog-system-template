package com.example.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.blog.config.UploadProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(UploadProperties.class)
public class BlogApplication {
  public static void main(String[] args) {
    SpringApplication.run(BlogApplication.class, args);
  }
}
