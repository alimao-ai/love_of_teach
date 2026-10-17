package com.loveofteach.platform.api.dto;

import com.loveofteach.platform.common.UserRole;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        UserRole role,
        String roleLabel,
        Long teacherId,
        String teacherNo,
        String teacherName
) {
}
