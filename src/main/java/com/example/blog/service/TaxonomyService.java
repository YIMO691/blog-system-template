package com.example.blog.service;

import com.example.blog.entity.Category;
import com.example.blog.entity.Tag;

import java.util.List;
import java.util.Set;

public interface TaxonomyService {
  List<Category> listCategories();
  List<Tag> listTags();
  Set<Tag> resolveTags(Set<String> tagNames);
  Category getCategoryOrNull(Long id);
  Category createCategory(String name);
}
