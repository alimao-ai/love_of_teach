package com.loveofteach.platform.api.dto;

import com.loveofteach.platform.common.SupervisionPlanStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SupervisionPlanItemResponse(
        Long id,
        Long offeringId,
        String courseCode,
        String courseName,
        String departmentName,
        String teacherName,
        String className,
        String semesterName,
        LocalDate plannedDate,
        LocalDate dueDate,
        SupervisionPlanStatus status,
        String statusLabel,
        boolean overdue,
        String note,
        String supervisorName,
        LocalDateTime completedAt
) {
}
