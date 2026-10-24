package com.loveofteach.platform.api.dto;

public record SupervisionPlanSummaryResponse(
        int total,
        int planned,
        int inProgress,
        int completed,
        int overdue,
        double progressRate
) {
}
