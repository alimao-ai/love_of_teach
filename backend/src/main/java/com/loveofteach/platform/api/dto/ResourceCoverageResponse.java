package com.loveofteach.platform.api.dto;

import java.util.List;

public record ResourceCoverageResponse(
        ResourceCoverageSummaryResponse summary,
        List<DepartmentResourceCoverageResponse> departmentBreakdown,
        List<CourseResourceCoverageResponse> courses
) {
}
