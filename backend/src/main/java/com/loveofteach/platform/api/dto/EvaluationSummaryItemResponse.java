package com.loveofteach.platform.api.dto;

import java.time.LocalDateTime;

public record EvaluationSummaryItemResponse(
        Long offeringId,
        String courseCode,
        String courseName,
        String teacherName,
        String departmentName,
        String semesterName,
        String className,
        int studentCount,
        double averageScore,
        int evaluationCount,
        String latestComment,
        LocalDateTime latestEvaluatedAt,
        boolean weak
) {
}
