package com.loveofteach.platform.api.dto;

public record TeacherResponse(
        Long id,
        String employeeNo,
        String name,
        String title,
        Long departmentId,
        String departmentName
) {
}
