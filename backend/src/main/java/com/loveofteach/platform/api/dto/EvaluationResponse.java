package com.loveofteach.platform.api.dto;

import java.time.LocalDateTime;

public record EvaluationResponse(
        Long id,
        Long offeringId,
        String courseCode,
        String courseName,
        String teacherName,
        String className,
        int score,
        String comment,
        String reviewerName,
        LocalDateTime evaluatedAt
) {
}
