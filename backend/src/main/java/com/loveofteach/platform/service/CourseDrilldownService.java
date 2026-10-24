package com.loveofteach.platform.service;

import com.loveofteach.platform.api.dto.CourseDrilldownResponse;
import com.loveofteach.platform.api.dto.CourseListItemResponse;
import com.loveofteach.platform.api.dto.DepartmentCourseDrilldownResponse;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.security.RequestUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 按学院汇总开课与学生数据，并钻取到与课程列表一致的课程明细。
 */
@Service
@RequiredArgsConstructor
public class CourseDrilldownService {

    private final CourseQueryService courseQueryService;
    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public CourseDrilldownResponse drilldown(
            Long semesterId,
            Long departmentId,
            Long teacherId,
            String keyword,
            RequestUser user
    ) {
        authorizationService.requireAnyRole(
                user, UserRole.SUPERVISOR, UserRole.DIRECTOR, UserRole.ADMIN);
        List<CourseListItemResponse> courses = courseQueryService.searchCourses(
                semesterId, departmentId, teacherId, keyword);
        Map<Long, List<CourseListItemResponse>> grouped = courses.stream()
                .collect(Collectors.groupingBy(
                        CourseListItemResponse::departmentId,
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<DepartmentCourseDrilldownResponse> departments = grouped.values().stream()
                .map(this::toDepartment)
                .sorted(Comparator.comparing(DepartmentCourseDrilldownResponse::departmentName))
                .toList();
        int supervised = (int) courses.stream()
                .filter(CourseListItemResponse::supervised)
                .count();
        long students = courses.stream()
                .mapToLong(CourseListItemResponse::studentCount)
                .sum();
        return new CourseDrilldownResponse(
                departments.size(),
                courses.size(),
                students,
                supervised,
                courses.size() - supervised,
                rate(supervised, courses.size()),
                departments);
    }

    private DepartmentCourseDrilldownResponse toDepartment(
            List<CourseListItemResponse> courses
    ) {
        CourseListItemResponse first = courses.get(0);
        int supervised = (int) courses.stream()
                .filter(CourseListItemResponse::supervised)
                .count();
        return new DepartmentCourseDrilldownResponse(
                first.departmentId(),
                first.departmentCode(),
                first.departmentName(),
                courses.size(),
                courses.stream().mapToLong(CourseListItemResponse::studentCount).sum(),
                supervised,
                courses.size() - supervised,
                rate(supervised, courses.size()),
                List.copyOf(courses));
    }

    private double rate(int supervised, int total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(supervised * 1000.0 / total) / 10.0;
    }
}
