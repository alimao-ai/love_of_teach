package com.loveofteach.platform.api.dto;

import java.util.List;

public record SupervisionPlanOverviewResponse(
        SupervisionPlanSummaryResponse summary,
        List<SupervisionPlanItemResponse> plans
) {
}
