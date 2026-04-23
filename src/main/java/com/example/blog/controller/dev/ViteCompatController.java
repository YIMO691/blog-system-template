package com.example.blog.controller.dev;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
public class ViteCompatController {

  @GetMapping(value = "/@vite/client", produces = "application/javascript")
  public ResponseEntity<String> viteClient() {
    return ResponseEntity.ok("export {};");
  }

  @GetMapping(value = "/@react-refresh", produces = "application/javascript")
  public ResponseEntity<String> reactRefresh() {
    return ResponseEntity.ok("export {};");
  }
}
