package com.loveofteach.platform.api;

import com.loveofteach.platform.api.dto.CourseListItemResponse;
import com.loveofteach.platform.api.dto.CourseOfferingDetailResponse;
import com.loveofteach.platform.api.dto.TeacherOverviewResponse;
import com.loveofteach.platform.service.CourseQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseQueryService courseQueryService;

    @GetMapping
    public List<CourseListItemResponse> listCourses(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String keyword
    ) {
        return courseQueryService.searchCourses(semesterId, departmentId, teacherId, keyword);
    }

    @GetMapping("/teacher-overview")
    public TeacherOverviewResponse teacherOverview(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long semesterId
    ) {
        return courseQueryService.teacherOverview(departmentId, teacherId, semesterId);
    }

    @GetMapping("/{offeringId}")
    public CourseOfferingDetailResponse getCourseDetail(@PathVariable Long offeringId) {
        return courseQueryService.getCourseDetail(offeringId);
    }
}
