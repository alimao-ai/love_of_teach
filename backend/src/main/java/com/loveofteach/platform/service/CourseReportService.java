package com.loveofteach.platform.service;

import com.loveofteach.platform.api.dto.ReportFileResponse;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.CourseEvaluation;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.domain.SupervisionRecord;
import com.loveofteach.platform.repository.CourseEvaluationRepository;
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.SupervisionRecordRepository;
import com.loveofteach.platform.repository.TeachingResourceRepository;
import com.loveofteach.platform.security.RequestUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 汇总开课、学生、资源、督导和评价数据并生成可下载统计表。
 */
@Service
@RequiredArgsConstructor
public class CourseReportService {

    private final CourseOfferingRepository offeringRepository;
    private final TeachingResourceRepository resourceRepository;
    private final SupervisionRecordRepository supervisionRepository;
    private final CourseEvaluationRepository evaluationRepository;
    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public ReportFileResponse exportOfferings(
            RequestUser user,
            Long semesterId,
            Long departmentId,
            Long teacherId
    ) {
        authorizationService.requireAnyRole(user, UserRole.DIRECTOR, UserRole.ADMIN);
        List<CourseOffering> offerings = offeringRepository.search(
                semesterId, departmentId, teacherId, null);
        Map<Long, Long> resourceCounts = resourceCounts(offerings);
        Set<Long> supervisedIds = supervisedIds(offerings);
        Map<Long, Double> evaluationAverages = evaluationAverages(offerings);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("课程代码,课程名称,院系,教师,学期,班级,学生人次,开课状态,资源数,督导状态,平均评分\n");
        for (CourseOffering offering : offerings) {
            csv.append(csvValue(offering.getCourse().getCode())).append(',')
                    .append(csvValue(offering.getCourse().getName())).append(',')
                    .append(csvValue(offering.getCourse().getDepartment().getName())).append(',')
                    .append(csvValue(offering.getTeacher().getName())).append(',')
                    .append(csvValue(offering.getSemester().getName())).append(',')
                    .append(csvValue(offering.getClassName())).append(',')
                    .append(offering.getStudentCount()).append(',')
                    .append(csvValue(offering.getStatus().getLabel())).append(',')
                    .append(resourceCounts.getOrDefault(offering.getId(), 0L)).append(',')
                    .append(supervisedIds.contains(offering.getId()) ? "已督导" : "待督导").append(',')
                    .append(evaluationAverages.containsKey(offering.getId())
                            ? String.format(java.util.Locale.ROOT, "%.1f", evaluationAverages.get(offering.getId()))
                            : "")
                    .append('\n');
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        return new ReportFileResponse(
                "course-offering-report-" + timestamp + ".csv",
                "text/csv;charset=UTF-8",
                csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private Map<Long, Long> resourceCounts(List<CourseOffering> offerings) {
        List<Long> offeringIds = offerings.stream().map(CourseOffering::getId).toList();
        if (offeringIds.isEmpty()) {
            return Map.of();
        }
        return resourceRepository.countByOfferingIds(offeringIds).stream()
                .collect(Collectors.toMap(
                        TeachingResourceRepository.OfferingResourceCount::getOfferingId,
                        TeachingResourceRepository.OfferingResourceCount::getResourceCount));
    }

    private Set<Long> supervisedIds(List<CourseOffering> offerings) {
        List<Long> offeringIds = offerings.stream().map(CourseOffering::getId).toList();
        if (offeringIds.isEmpty()) {
            return Set.of();
        }
        return supervisionRepository.findByOfferingIdIn(offeringIds).stream()
                .map(SupervisionRecord::getOffering)
                .map(CourseOffering::getId)
                .collect(Collectors.toSet());
    }

    private Map<Long, Double> evaluationAverages(List<CourseOffering> offerings) {
        List<Long> offeringIds = offerings.stream().map(CourseOffering::getId).toList();
        if (offeringIds.isEmpty()) {
            return Map.of();
        }
        return evaluationRepository.findByOfferingIdIn(offeringIds).stream()
                .collect(Collectors.groupingBy(
                        evaluation -> evaluation.getOffering().getId(),
                        Collectors.averagingInt(CourseEvaluation::getScore)));
    }

    private String csvValue(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
