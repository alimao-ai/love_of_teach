package com.loveofteach.platform.api.dto;

import com.loveofteach.platform.common.OfferingStatus;

import java.math.BigDecimal;
import java.util.List;

public record CourseOfferingDetailResponse(
        Long offeringId,
        String courseCode,
        String courseName,
        BigDecimal credits,
        String description,
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
        int supervisionCount,
        List<CourseResourceResponse> resources,
        List<ResourceChapterResponse> chapters,
        String resourceEmptyMessage
) {
}
