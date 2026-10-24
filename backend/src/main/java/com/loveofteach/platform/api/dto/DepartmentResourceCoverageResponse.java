package com.loveofteach.platform.api.dto;

public record DepartmentResourceCoverageResponse(
        Long departmentId,
        String departmentCode,
        String departmentName,
        int offeringCount,
        int coveredOfferingCount,
        int uncoveredOfferingCount,
        double coverageRate
) {
}
