package com.loveofteach.platform.api.dto;

public record DepartmentCoverageResponse(
        Long departmentId,
        String departmentCode,
        String departmentName,
        int offeringCount,
        int supervisedCount,
        int unsupervisedCount,
        double coverageRate
) {
}
