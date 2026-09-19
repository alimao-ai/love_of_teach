package com.loveofteach.platform.service;

import com.loveofteach.platform.api.dto.CourseListItemResponse;
import com.loveofteach.platform.api.dto.CourseOfferingDetailResponse;
import com.loveofteach.platform.api.dto.CourseResourceResponse;
import com.loveofteach.platform.api.dto.CoverageSummaryResponse;
import com.loveofteach.platform.api.dto.DepartmentCoverageResponse;
import com.loveofteach.platform.api.dto.SupervisionCoverageResponse;
import com.loveofteach.platform.api.dto.TeacherCourseGroupResponse;
import com.loveofteach.platform.api.dto.TeacherOverviewResponse;
import com.loveofteach.platform.common.BusinessException;
import com.loveofteach.platform.common.ResourceType;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.domain.Department;
import com.loveofteach.platform.domain.SupervisionRecord;
import com.loveofteach.platform.domain.Teacher;
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.SupervisionRecordRepository;
import com.loveofteach.platform.repository.TeacherRepository;
import com.loveofteach.platform.repository.TeachingResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseQueryService {

    private final CourseOfferingRepository offeringRepository;
    private final TeachingResourceRepository resourceRepository;
    private final SupervisionRecordRepository supervisionRepository;
    private final TeacherRepository teacherRepository;

    @Transactional(readOnly = true)
    public List<CourseListItemResponse> searchCourses(
            Long semesterId,
            Long departmentId,
            Long teacherId,
            String keyword
    ) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return toListItems(offeringRepository.search(
                semesterId, departmentId, teacherId, normalizedKeyword));
    }

    @Transactional(readOnly = true)
    public TeacherOverviewResponse teacherOverview(
            Long departmentId,
            Long teacherId,
            Long semesterId
    ) {
        List<Teacher> teachers;
        if (teacherId != null) {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new BusinessException("教师不存在", HttpStatus.NOT_FOUND));
            teachers = List.of(teacher);
        } else if (departmentId != null) {
            teachers = teacherRepository.findByDepartmentIdOrderByNameAsc(departmentId);
        } else {
            teachers = teacherRepository.findAllByOrderByNameAsc();
        }

        List<CourseListItemResponse> courses = toListItems(offeringRepository.search(
                semesterId, departmentId, teacherId, null));
        Map<Long, List<CourseListItemResponse>> coursesByTeacher = courses.stream()
                .collect(Collectors.groupingBy(CourseListItemResponse::teacherId, LinkedHashMap::new, Collectors.toList()));

        List<TeacherCourseGroupResponse> groups = teachers.stream()
                .map(teacher -> new TeacherCourseGroupResponse(
                        teacher.getId(),
                        teacher.getEmployeeNo(),
                        teacher.getName(),
                        teacher.getTitle(),
                        teacher.getDepartment().getName(),
                        coursesByTeacher.getOrDefault(teacher.getId(), List.of())))
                .toList();

        String emptyMessage = groups.isEmpty() ? "当前筛选条件下暂无教师数据" : null;
        return new TeacherOverviewResponse(groups, groups.size(), courses.size(), emptyMessage);
    }

    @Transactional(readOnly = true)
    public CourseOfferingDetailResponse getCourseDetail(Long offeringId) {
        CourseOffering offering = offeringRepository.findDetailedById(offeringId)
                .orElseThrow(() -> new BusinessException("开课记录不存在", HttpStatus.NOT_FOUND));
        long supervisionCount = supervisionRepository.countByOfferingId(offeringId);
        List<CourseResourceResponse> resources = resourceRepository
                .findByOfferingIdOrderByUploadedAtDesc(offeringId)
                .stream()
                .map(resource -> new CourseResourceResponse(
                        resource.getId(),
                        resource.getName(),
                        resource.getResourceType(),
                        resourceTypeLabel(resource.getResourceType()),
                        resource.getUrl(),
                        resource.getUploadedAt()))
                .toList();

        return new CourseOfferingDetailResponse(
                offering.getId(),
                offering.getCourse().getCode(),
                offering.getCourse().getName(),
                offering.getCourse().getCredits(),
                offering.getCourse().getDescription(),
                offering.getCourse().getDepartment().getCode(),
                offering.getCourse().getDepartment().getName(),
                offering.getTeacher().getId(),
                offering.getTeacher().getName(),
                offering.getSemester().getId(),
                offering.getSemester().getName(),
                offering.getClassName(),
                offering.getStudentCount(),
                offering.getStatus(),
                offering.getStatus().getLabel(),
                supervisionCount > 0,
                Math.toIntExact(supervisionCount),
                resources,
                resources.isEmpty() ? "该课程暂未上传教学资源" : null);
    }

    @Transactional(readOnly = true)
    public SupervisionCoverageResponse getCoverage(Long semesterId, Long departmentId) {
        List<CourseOffering> offerings = offeringRepository.search(semesterId, departmentId, null, null);
        Set<Long> supervisedIds = supervisedOfferingIds(offerings);
        List<CourseListItemResponse> courseItems = buildListItems(offerings, supervisedIds);

        int offeringCount = offerings.size();
        int supervisedCount = (int) offerings.stream()
                .filter(offering -> supervisedIds.contains(offering.getId()))
                .count();
        long studentCount = offerings.stream().mapToLong(CourseOffering::getStudentCount).sum();
        CoverageSummaryResponse summary = new CoverageSummaryResponse(
                offeringCount,
                supervisedCount,
                offeringCount - supervisedCount,
                coverageRate(supervisedCount, offeringCount),
                studentCount);

        Map<Long, List<CourseOffering>> offeringsByDepartment = offerings.stream()
                .collect(Collectors.groupingBy(
                        offering -> offering.getCourse().getDepartment().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<DepartmentCoverageResponse> departmentBreakdown = offeringsByDepartment.values().stream()
                .map(departmentOfferings -> {
                    Department department = departmentOfferings.get(0).getCourse().getDepartment();
                    int total = departmentOfferings.size();
                    int supervised = (int) departmentOfferings.stream()
                            .filter(offering -> supervisedIds.contains(offering.getId()))
                            .count();
                    return new DepartmentCoverageResponse(
                            department.getId(),
                            department.getCode(),
                            department.getName(),
                            total,
                            supervised,
                            total - supervised,
                            coverageRate(supervised, total));
                })
                .sorted(Comparator.comparing(DepartmentCoverageResponse::departmentName))
                .toList();

        return new SupervisionCoverageResponse(summary, departmentBreakdown, courseItems);
    }

    private List<CourseListItemResponse> toListItems(List<CourseOffering> offerings) {
        return buildListItems(offerings, supervisedOfferingIds(offerings));
    }

    private List<CourseListItemResponse> buildListItems(
            List<CourseOffering> offerings,
            Set<Long> supervisedIds
    ) {
        Map<Long, Long> resourceCounts = offerings.stream()
                .map(CourseOffering::getId)
                .collect(Collectors.toMap(Function.identity(), resourceRepository::countByOfferingId));

        return offerings.stream()
                .map(offering -> new CourseListItemResponse(
                        offering.getId(),
                        offering.getCourse().getCode(),
                        offering.getCourse().getName(),
                        offering.getCourse().getDepartment().getCode(),
                        offering.getCourse().getDepartment().getName(),
                        offering.getTeacher().getId(),
                        offering.getTeacher().getName(),
                        offering.getSemester().getId(),
                        offering.getSemester().getName(),
                        offering.getClassName(),
                        offering.getStudentCount(),
                        offering.getStatus(),
                        offering.getStatus().getLabel(),
                        supervisedIds.contains(offering.getId()),
                        resourceCounts.getOrDefault(offering.getId(), 0L)))
                .toList();
    }

    private Set<Long> supervisedOfferingIds(List<CourseOffering> offerings) {
        List<Long> offeringIds = offerings.stream().map(CourseOffering::getId).toList();
        if (offeringIds.isEmpty()) {
            return Set.of();
        }
        return supervisionRepository.findByOfferingIdIn(offeringIds).stream()
                .map(SupervisionRecord::getOffering)
                .map(CourseOffering::getId)
                .collect(Collectors.toSet());
    }

    private double coverageRate(int supervised, int total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(supervised * 1000.0 / total) / 10.0;
    }

    private String resourceTypeLabel(ResourceType type) {
        return switch (type) {
            case DOCUMENT -> "文档";
            case SLIDES -> "课件";
            case VIDEO -> "视频";
            case LINK -> "链接";
        };
    }
}
