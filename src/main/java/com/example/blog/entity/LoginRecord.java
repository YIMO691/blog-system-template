package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "login_records", indexes = {
    @Index(name = "idx_login_records_user", columnList = "user_id"),
    @Index(name = "idx_login_records_time", columnList = "time")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LoginRecord {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = true)
  private User user;

  @Column(nullable = false)
  private Instant time;

  @Column(length = 64)
  private String ip;

  @Column(length = 512)
  private String ua;

  @Column(nullable = false)
  private boolean success;
}
