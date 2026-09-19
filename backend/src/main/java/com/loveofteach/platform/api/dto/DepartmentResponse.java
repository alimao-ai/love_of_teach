package com.loveofteach.platform.api.dto;

public record DepartmentResponse(
        Long id,
        String code,
        String name,
        boolean active,
        long teacherCount
) {
}
