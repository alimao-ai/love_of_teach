package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.CourseNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CourseNotificationRepository extends JpaRepository<CourseNotification, Long> {

    List<CourseNotification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    long countByRecipientIdAndReadAtIsNull(Long recipientId);

    @Modifying
    @Query("""
            update CourseNotification notification
            set notification.readAt = :readAt
            where notification.recipient.id = :recipientId and notification.readAt is null
            """)
    int markAllRead(
            @Param("recipientId") Long recipientId,
            @Param("readAt") LocalDateTime readAt);
}
