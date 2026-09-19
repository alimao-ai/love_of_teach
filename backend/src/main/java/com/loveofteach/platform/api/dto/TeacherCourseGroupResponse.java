package com.loveofteach.platform.api.dto;

import java.util.List;

public record TeacherCourseGroupResponse(
        Long teacherId,
        String employeeNo,
        String teacherName,
        String title,
        String departmentName,
        List<CourseListItemResponse> courses
) {
}
