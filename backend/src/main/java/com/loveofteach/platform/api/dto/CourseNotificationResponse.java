package com.loveofteach.platform.api.dto;

import java.time.LocalDateTime;

public record CourseNotificationResponse(
        Long id,
        Long courseId,
        String courseCode,
        String courseName,
        String title,
        String message,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        boolean read
) {
}
