package com.example.blog.repository;

import com.example.blog.entity.LoginRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface LoginRecordRepository extends JpaRepository<LoginRecord, Long> {
  Page<LoginRecord> findByUserIdOrderByTimeDesc(Long userId, Pageable pageable);
  Page<LoginRecord> findBySuccessFalseOrderByTimeDesc(Pageable pageable);
  long countBySuccessFalseAndTimeAfter(Instant time);
}
