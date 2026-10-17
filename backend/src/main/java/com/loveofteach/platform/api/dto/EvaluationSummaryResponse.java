package com.loveofteach.platform.api.dto;

import java.util.List;

public record EvaluationSummaryResponse(
        int evaluatedOfferingCount,
        int evaluationCount,
        double overallAverageScore,
        int weakOfferingCount,
        List<EvaluationSummaryItemResponse> items
) {
}
