package com.example.blog.repository;

import com.example.blog.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
  long countByRecipientIdAndReadFalse(Long recipientId);
  
  @Modifying
  @Query("update Notification n set n.read = true where n.recipient.id = :uid and n.read = false")
  int markAllReadByRecipientId(@Param("uid") Long recipientId);
  
  @Modifying
  @Query("delete from Notification n where n.recipient.id = :uid and n.read = true")
  int deleteReadByRecipientId(@Param("uid") Long recipientId);
}
