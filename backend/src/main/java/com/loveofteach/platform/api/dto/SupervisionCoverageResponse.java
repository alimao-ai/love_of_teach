package com.loveofteach.platform.api.dto;

import java.util.List;

public record SupervisionCoverageResponse(
        CoverageSummaryResponse summary,
        List<DepartmentCoverageResponse> departmentBreakdown,
        List<CourseListItemResponse> courses
) {
}
