package com.loveofteach.platform.api.dto;

import com.loveofteach.platform.common.OfferingStatus;

public record CourseListItemResponse(
        Long offeringId,
        String courseCode,
        String courseName,
        Long departmentId,
        String departmentCode,
        String departmentName,
        Long teacherId,
        String teacherName,
        Long semesterId,
        String semesterName,
        String className,
        int studentCount,
        OfferingStatus status,
        String statusLabel,
        boolean supervised,
        long resourceCount
) {
}
