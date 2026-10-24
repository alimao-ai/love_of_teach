package com.loveofteach.platform.service;

import com.loveofteach.platform.api.dto.CourseResourceCoverageResponse;
import com.loveofteach.platform.api.dto.DepartmentResourceCoverageResponse;
import com.loveofteach.platform.api.dto.ResourceCoverageResponse;
import com.loveofteach.platform.api.dto.ResourceCoverageSummaryResponse;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.domain.Department;
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.TeachingResourceRepository;
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
 * 计算课程资源覆盖率和低覆盖课程，用于督促资源建设。
 */
@Service
@RequiredArgsConstructor
public class ResourceCoverageService {

    private final CourseOfferingRepository offeringRepository;
    private final TeachingResourceRepository resourceRepository;
    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public ResourceCoverageResponse coverage(
            Long semesterId,
            Long departmentId,
            RequestUser user
    ) {
        authorizationService.requireAnyRole(
                user, UserRole.SUPERVISOR, UserRole.DIRECTOR, UserRole.ADMIN);
        List<CourseOffering> offerings =
                offeringRepository.search(semesterId, departmentId, null, null);
        Map<Long, Long> counts = offerings.isEmpty()
                ? Map.of()
                : resourceRepository.countByOfferingIds(
                        offerings.stream().map(CourseOffering::getId).toList())
                .stream()
                .collect(Collectors.toMap(
                        TeachingResourceRepository.OfferingResourceCount::getOfferingId,
                        TeachingResourceRepository.OfferingResourceCount::getResourceCount));
        int covered = (int) offerings.stream()
                .filter(offering -> counts.getOrDefault(offering.getId(), 0L) > 0)
                .count();
        int totalResources = counts.values().stream().mapToInt(Long::intValue).sum();
        ResourceCoverageSummaryResponse summary = new ResourceCoverageSummaryResponse(
                offerings.size(),
                covered,
                offerings.size() - covered,
                rate(covered, offerings.size()),
                totalResources);

        Map<Long, List<CourseOffering>> grouped = offerings.stream()
                .collect(Collectors.groupingBy(
                        offering -> offering.getCourse().getDepartment().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<DepartmentResourceCoverageResponse> departments = grouped.values().stream()
                .map(items -> {
                    Department department = items.get(0).getCourse().getDepartment();
                    int departmentCovered = (int) items.stream()
                            .filter(item -> counts.getOrDefault(item.getId(), 0L) > 0)
                            .count();
                    return new DepartmentResourceCoverageResponse(
                            department.getId(),
                            department.getCode(),
                            department.getName(),
                            items.size(),
                            departmentCovered,
                            items.size() - departmentCovered,
                            rate(departmentCovered, items.size()));
                })
                .sorted(Comparator.comparing(DepartmentResourceCoverageResponse::departmentName))
                .toList();

        List<CourseResourceCoverageResponse> courses = offerings.stream()
                .map(offering -> {
                    int count = counts.getOrDefault(offering.getId(), 0L).intValue();
                    return new CourseResourceCoverageResponse(
                            offering.getId(),
                            offering.getCourse().getCode(),
                            offering.getCourse().getName(),
                            offering.getCourse().getDepartment().getName(),
                            offering.getTeacher().getName(),
                            offering.getClassName(),
                            count,
                            count > 0);
                })
                .sorted(Comparator
                        .comparing(CourseResourceCoverageResponse::covered)
                        .thenComparing(CourseResourceCoverageResponse::courseCode))
                .toList();
        return new ResourceCoverageResponse(summary, departments, courses);
    }

    private double rate(int covered, int total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(covered * 1000.0 / total) / 10.0;
    }
}
