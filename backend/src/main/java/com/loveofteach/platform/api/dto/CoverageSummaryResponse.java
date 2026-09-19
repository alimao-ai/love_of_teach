package com.loveofteach.platform.api.dto;

public record CoverageSummaryResponse(
        int offeringCount,
        int supervisedCount,
        int unsupervisedCount,
        double coverageRate,
        long studentCount
) {
}
