package com.loveofteach.platform.api.dto;

public record CourseSubscriptionResponse(
        Long courseId,
        String courseCode,
        String courseName,
        String teacherName,
        boolean subscribed
) {
}
