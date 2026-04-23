package com.example.blog.repository;

import com.example.blog.entity.ActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {
  @EntityGraph(attributePaths = {"user"})
  Page<ActionLog> findByUserIdOrderByTimeDesc(Long userId, Pageable pageable);

  @EntityGraph(attributePaths = {"user"})
  Page<ActionLog> findAllByOrderByTimeDesc(Pageable pageable);
}
