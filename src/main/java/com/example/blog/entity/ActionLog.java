package com.example.blog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "action_logs", indexes = {
    @Index(name = "idx_action_logs_user", columnList = "user_id"),
    @Index(name = "idx_action_logs_time", columnList = "time")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ActionLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  private Instant time;

  @Column(length = 128, nullable = false)
  private String action;

  @Column(length = 512)
  private String detail;
}
