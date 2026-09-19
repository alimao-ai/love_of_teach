package com.loveofteach.platform.api.dto;

import java.util.List;

public record TeacherOverviewResponse(
        List<TeacherCourseGroupResponse> teachers,
        int teacherCount,
        int courseCount,
        String emptyMessage
) {
}
