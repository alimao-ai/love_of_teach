package com.loveofteach.platform.api.dto;

public record CourseResourceCoverageResponse(
        Long offeringId,
        String courseCode,
        String courseName,
        String departmentName,
        String teacherName,
        String className,
        int resourceCount,
        boolean covered
) {
}
