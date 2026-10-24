package com.loveofteach.platform.service;

import com.loveofteach.platform.api.dto.SupervisionPlanItemResponse;
import com.loveofteach.platform.api.dto.SupervisionPlanOverviewResponse;
import com.loveofteach.platform.api.dto.SupervisionPlanRequest;
import com.loveofteach.platform.api.dto.SupervisionPlanSummaryResponse;
import com.loveofteach.platform.common.BusinessException;
import com.loveofteach.platform.common.SupervisionPlanStatus;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.domain.SupervisionPlan;
import com.loveofteach.platform.domain.SupervisionRecord;
import com.loveofteach.platform.domain.UserAccount;
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.SupervisionPlanRepository;
import com.loveofteach.platform.repository.SupervisionRecordRepository;
import com.loveofteach.platform.repository.UserAccountRepository;
import com.loveofteach.platform.security.RequestUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建和维护听评课计划，并把完成状态与督导记录联动。
 */
@Service
@RequiredArgsConstructor
public class SupervisionPlanService {

    private final SupervisionPlanRepository planRepository;
    private final CourseOfferingRepository offeringRepository;
    private final SupervisionRecordRepository supervisionRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public SupervisionPlanOverviewResponse overview(
            Long semesterId,
            Long departmentId,
            RequestUser user
    ) {
        authorizationService.requireAnyRole(
                user, UserRole.SUPERVISOR, UserRole.DIRECTOR, UserRole.ADMIN);
        List<SupervisionPlan> plans = planRepository.search(semesterId, departmentId);
        LocalDate today = LocalDate.now();
        List<SupervisionPlanItemResponse> items = plans.stream()
                .map(plan -> toResponse(plan, today))
                .toList();
        int planned = count(items, SupervisionPlanStatus.PLANNED);
        int inProgress = count(items, SupervisionPlanStatus.IN_PROGRESS);
        int completed = count(items, SupervisionPlanStatus.COMPLETED);
        int overdue = (int) items.stream().filter(SupervisionPlanItemResponse::overdue).count();
        int activeTotal = planned + inProgress + completed;
        double progressRate = activeTotal == 0
                ? 0.0
                : Math.round(completed * 1000.0 / activeTotal) / 10.0;
        return new SupervisionPlanOverviewResponse(
                new SupervisionPlanSummaryResponse(
                        activeTotal, planned, inProgress, completed, overdue, progressRate),
                items);
    }

    @Transactional
    public SupervisionPlanItemResponse create(
            SupervisionPlanRequest request,
            RequestUser user
    ) {
        authorizationService.requireAnyRole(
                user, UserRole.SUPERVISOR, UserRole.DIRECTOR, UserRole.ADMIN);
        if (request.dueDate().isBefore(request.plannedDate())) {
            throw new BusinessException("截止日期不能早于计划日期");
        }
        CourseOffering offering = offeringRepository.findDetailedById(request.offeringId())
                .orElseThrow(() -> new BusinessException("开课记录不存在", HttpStatus.NOT_FOUND));
        UserAccount supervisor = userAccountRepository.findById(user.userId())
                .orElseThrow(() -> new BusinessException("用户不存在", HttpStatus.UNAUTHORIZED));
        SupervisionPlan plan = planRepository.save(SupervisionPlan.builder()
                .offering(offering)
                .supervisor(supervisor)
                .plannedDate(request.plannedDate())
                .dueDate(request.dueDate())
                .status(SupervisionPlanStatus.PLANNED)
                .note(request.note())
                .build());
        return toResponse(plan, LocalDate.now());
    }

    @Transactional
    public SupervisionPlanItemResponse updateStatus(
            Long planId,
            SupervisionPlanStatus status,
            RequestUser user
    ) {
        authorizationService.requireAnyRole(
                user, UserRole.SUPERVISOR, UserRole.DIRECTOR, UserRole.ADMIN);
        SupervisionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("听评课计划不存在", HttpStatus.NOT_FOUND));
        plan.setStatus(status);
        if (status == SupervisionPlanStatus.COMPLETED) {
            // Serialize completion of multiple plans for the same offering.
            offeringRepository.findDetailedByIdForUpdate(plan.getOffering().getId())
                    .orElseThrow(() -> new BusinessException(
                            "开课记录不存在", HttpStatus.NOT_FOUND));
            plan.setCompletedAt(LocalDateTime.now());
            if (!supervisionRepository.existsByOfferingId(plan.getOffering().getId())) {
                supervisionRepository.save(SupervisionRecord.builder()
                        .offering(plan.getOffering())
                        .supervisor(user.displayName())
                        .supervisedAt(LocalDate.now())
                        .build());
            }
        } else {
            plan.setCompletedAt(null);
        }
        return toResponse(planRepository.save(plan), LocalDate.now());
    }

    private int count(
            List<SupervisionPlanItemResponse> items,
            SupervisionPlanStatus status
    ) {
        return (int) items.stream().filter(item -> item.status() == status).count();
    }

    private SupervisionPlanItemResponse toResponse(SupervisionPlan plan, LocalDate today) {
        CourseOffering offering = plan.getOffering();
        boolean overdue = plan.getDueDate().isBefore(today)
                && plan.getStatus() != SupervisionPlanStatus.COMPLETED
                && plan.getStatus() != SupervisionPlanStatus.CANCELLED;
        return new SupervisionPlanItemResponse(
                plan.getId(),
                offering.getId(),
                offering.getCourse().getCode(),
                offering.getCourse().getName(),
                offering.getCourse().getDepartment().getName(),
                offering.getTeacher().getName(),
                offering.getClassName(),
                offering.getSemester().getName(),
                plan.getPlannedDate(),
                plan.getDueDate(),
                plan.getStatus(),
                plan.getStatus().getLabel(),
                overdue,
                plan.getNote(),
                plan.getSupervisor().getDisplayName(),
                plan.getCompletedAt());
    }
}
