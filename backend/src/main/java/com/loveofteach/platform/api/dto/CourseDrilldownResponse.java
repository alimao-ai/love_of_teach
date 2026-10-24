package com.loveofteach.platform.api.dto;

import java.util.List;

public record CourseDrilldownResponse(
        int departmentCount,
        int offeringCount,
        long studentCount,
        int supervisedOfferingCount,
        int unsupervisedOfferingCount,
        double supervisionCoverageRate,
        List<DepartmentCourseDrilldownResponse> departments
) {
}
