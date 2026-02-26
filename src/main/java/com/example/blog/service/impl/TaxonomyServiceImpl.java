package com.example.blog.service.impl;

import com.example.blog.entity.Category;
import com.example.blog.entity.Tag;
import com.example.blog.repository.CategoryRepository;
import com.example.blog.repository.TagRepository;
import com.example.blog.service.TaxonomyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxonomyServiceImpl implements TaxonomyService {

  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;

  @Override
  public List<Category> listCategories() {
    return categoryRepository.findAll();
  }

  @Override
  public List<Tag> listTags() {
    return tagRepository.findAll();
  }

  @Override
  public Set<Tag> resolveTags(Set<String> tagNames) {
    if (tagNames == null || tagNames.isEmpty()) return new LinkedHashSet<>();
    Set<String> normalized = tagNames.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .map(String::toLowerCase)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    Set<Tag> result = new LinkedHashSet<>();
    for (String name : normalized) {
      Tag tag = tagRepository.findByNameIgnoreCase(name)
          .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
      result.add(tag);
    }
    return result;
  }

  @Override
  public Category getCategoryOrNull(Long id) {
    if (id == null) return null;
    return categoryRepository.findById(id).orElse(null);
  }

  @Override
  public Category createCategory(String name) {
    if (name == null || name.isBlank()) return null;
    return categoryRepository.findByNameIgnoreCase(name)
        .orElseGet(() -> categoryRepository.save(Category.builder().name(name).build()));
  }
}
