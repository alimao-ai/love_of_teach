package com.loveofteach.platform.api.dto;

public record ResourceCoverageSummaryResponse(
        int offeringCount,
        int coveredOfferingCount,
        int uncoveredOfferingCount,
        double coverageRate,
        int resourceCount
) {
}
