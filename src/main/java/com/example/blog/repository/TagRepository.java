package com.example.blog.repository;

import com.example.blog.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
  Optional<Tag> findByNameIgnoreCase(String name);
  boolean existsByNameIgnoreCase(String name);

  @org.springframework.data.jpa.repository.Query("""
      SELECT t.name, COUNT(a)
      FROM Article a JOIN a.tags t
      WHERE a.published = true
      GROUP BY t.name
      ORDER BY COUNT(a) DESC
      """)
  java.util.List<Object[]> countTagUsage();
}
