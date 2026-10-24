package com.loveofteach.platform.api.dto;

import java.util.List;

public record DepartmentCourseDrilldownResponse(
        Long departmentId,
        String departmentCode,
        String departmentName,
        int offeringCount,
        long studentCount,
        int supervisedOfferingCount,
        int unsupervisedOfferingCount,
        double supervisionCoverageRate,
        List<CourseListItemResponse> courses
) {
}
