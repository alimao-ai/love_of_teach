package com.loveofteach.platform.api.dto;

import java.util.List;

public record TeacherFeedbackResponse(
        Long teacherId,
        String teacherName,
        int offeringCount,
        int evaluationCount,
        double overallAverageScore,
        List<EvaluationSummaryItemResponse> courses,
        List<EvaluationResponse> feedback
) {
}
