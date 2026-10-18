package com.loveofteach.platform.service;

import com.loveofteach.platform.api.dto.EvaluationResponse;
import com.loveofteach.platform.api.dto.EvaluationSummaryItemResponse;
import com.loveofteach.platform.api.dto.EvaluationSummaryResponse;
import com.loveofteach.platform.api.dto.SubmitEvaluationRequest;
import com.loveofteach.platform.api.dto.TeacherFeedbackResponse;
import com.loveofteach.platform.common.BusinessException;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.CourseEvaluation;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.domain.SupervisionRecord;
import com.loveofteach.platform.domain.UserAccount;
import com.loveofteach.platform.repository.CourseEvaluationRepository;
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.SupervisionRecordRepository;
import com.loveofteach.platform.repository.UserAccountRepository;
import com.loveofteach.platform.security.RequestUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 处理课程评价提交、评价汇总和教师本人反馈。
 * 首次评价会自动生成督导记录，从而联动督导覆盖率。
 */
@Service
@RequiredArgsConstructor
public class CourseEvaluationService {

    private final CourseEvaluationRepository evaluationRepository;
    private final CourseOfferingRepository offeringRepository;
    private final SupervisionRecordRepository supervisionRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthorizationService authorizationService;

    @Transactional
    public EvaluationResponse submit(SubmitEvaluationRequest request, RequestUser user) {
        authorizationService.requireAnyRole(user, UserRole.SUPERVISOR, UserRole.DIRECTOR);
        CourseOffering offering = offeringRepository.findDetailedByIdForUpdate(request.offeringId())
                .orElseThrow(() -> new BusinessException("开课记录不存在", HttpStatus.NOT_FOUND));
        UserAccount reviewer = userAccountRepository.findById(user.userId())
                .orElseThrow(() -> new BusinessException("用户不存在", HttpStatus.UNAUTHORIZED));
        CourseEvaluation evaluation = evaluationRepository
                .findByOfferingIdAndReviewerId(offering.getId(), reviewer.getId())
                .orElseGet(() -> CourseEvaluation.builder()
                        .offering(offering)
                        .reviewer(reviewer)
                        .build());
        evaluation.setScore(request.score());
        evaluation.setComment(request.comment().trim());
        evaluation.setEvaluatedAt(LocalDateTime.now());
        CourseEvaluation saved = evaluationRepository.save(evaluation);
        if (supervisionRepository.countByOfferingId(offering.getId()) == 0) {
            supervisionRepository.save(SupervisionRecord.builder()
                    .offering(offering)
                    .supervisor(reviewer.getDisplayName())
                    .supervisedAt(LocalDate.now())
                    .build());
        }
        return toEvaluationResponse(saved);
    }

    @Transactional(readOnly = true)
    public EvaluationSummaryResponse summary(
            RequestUser user,
            Long semesterId,
            Long departmentId
    ) {
        authorizationService.requireAnyRole(
                user, UserRole.SUPERVISOR, UserRole.DIRECTOR, UserRole.ADMIN);
        List<CourseOffering> offerings = offeringRepository.search(
                semesterId, departmentId, null, null);
        List<CourseEvaluation> evaluations = evaluationsFor(offerings);
        Map<Long, List<CourseEvaluation>> grouped = evaluations.stream()
                .collect(Collectors.groupingBy(evaluation -> evaluation.getOffering().getId()));
        List<EvaluationSummaryItemResponse> items = offerings.stream()
                .filter(offering -> grouped.containsKey(offering.getId()))
                .map(offering -> toSummaryItem(offering, grouped.get(offering.getId())))
                .sorted(Comparator.comparingDouble(EvaluationSummaryItemResponse::averageScore))
                .toList();
        double overallAverage = evaluations.stream()
                .mapToInt(CourseEvaluation::getScore)
                .average()
                .orElse(0.0);
        return new EvaluationSummaryResponse(
                items.size(),
                evaluations.size(),
                round(overallAverage),
                (int) items.stream().filter(EvaluationSummaryItemResponse::weak).count(),
                items);
    }

    @Transactional(readOnly = true)
    public TeacherFeedbackResponse myFeedback(RequestUser user, Long semesterId) {
        authorizationService.requireTeacherProfile(user);
        List<CourseOffering> offerings = offeringRepository.search(
                semesterId, null, user.teacherId(), null);
        List<CourseEvaluation> evaluations = evaluationsFor(offerings);
        Map<Long, List<CourseEvaluation>> grouped = evaluations.stream()
                .collect(Collectors.groupingBy(evaluation -> evaluation.getOffering().getId()));
        List<EvaluationSummaryItemResponse> courses = offerings.stream()
                .map(offering -> grouped.containsKey(offering.getId())
                        ? toSummaryItem(offering, grouped.get(offering.getId()))
                        : toEmptySummaryItem(offering))
                .toList();
        double overallAverage = evaluations.stream()
                .mapToInt(CourseEvaluation::getScore)
                .average()
                .orElse(0.0);
        return new TeacherFeedbackResponse(
                user.teacherId(),
                user.displayName(),
                offerings.size(),
                evaluations.size(),
                round(overallAverage),
                courses,
                evaluations.stream().map(this::toEvaluationResponse).toList());
    }

    private List<CourseEvaluation> evaluationsFor(List<CourseOffering> offerings) {
        List<Long> offeringIds = offerings.stream().map(CourseOffering::getId).toList();
        if (offeringIds.isEmpty()) {
            return List.of();
        }
        return evaluationRepository.findByOfferingIdInWithReviewer(offeringIds);
    }

    private EvaluationSummaryItemResponse toSummaryItem(
            CourseOffering offering,
            List<CourseEvaluation> evaluations
    ) {
        CourseEvaluation latest = evaluations.stream()
                .max(Comparator.comparing(CourseEvaluation::getEvaluatedAt))
                .orElseThrow();
        double average = evaluations.stream()
                .mapToInt(CourseEvaluation::getScore)
                .average()
                .orElse(0.0);
        double rounded = round(average);
        return new EvaluationSummaryItemResponse(
                offering.getId(),
                offering.getCourse().getCode(),
                offering.getCourse().getName(),
                offering.getTeacher().getName(),
                offering.getCourse().getDepartment().getName(),
                offering.getSemester().getName(),
                offering.getClassName(),
                offering.getStudentCount(),
                rounded,
                evaluations.size(),
                latest.getComment(),
                latest.getEvaluatedAt(),
                rounded < 70.0);
    }

    private EvaluationSummaryItemResponse toEmptySummaryItem(CourseOffering offering) {
        return new EvaluationSummaryItemResponse(
                offering.getId(),
                offering.getCourse().getCode(),
                offering.getCourse().getName(),
                offering.getTeacher().getName(),
                offering.getCourse().getDepartment().getName(),
                offering.getSemester().getName(),
                offering.getClassName(),
                offering.getStudentCount(),
                0.0,
                0,
                null,
                null,
                true);
    }

    private EvaluationResponse toEvaluationResponse(CourseEvaluation evaluation) {
        CourseOffering offering = evaluation.getOffering();
        return new EvaluationResponse(
                evaluation.getId(),
                offering.getId(),
                offering.getCourse().getCode(),
                offering.getCourse().getName(),
                offering.getTeacher().getName(),
                offering.getClassName(),
                evaluation.getScore(),
                evaluation.getComment(),
                evaluation.getReviewer().getDisplayName(),
                evaluation.getEvaluatedAt());
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
