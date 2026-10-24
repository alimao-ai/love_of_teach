package com.loveofteach.platform.api;

import com.loveofteach.platform.api.dto.SupervisionCoverageResponse;
import com.loveofteach.platform.api.dto.SupervisionPlanItemResponse;
import com.loveofteach.platform.api.dto.SupervisionPlanOverviewResponse;
import com.loveofteach.platform.api.dto.SupervisionPlanRequest;
import com.loveofteach.platform.api.dto.SupervisionPlanStatusRequest;
import com.loveofteach.platform.security.AuthContext;
import com.loveofteach.platform.service.SupervisionPlanService;
import com.loveofteach.platform.service.CourseQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supervision")
public class SupervisionController {

    private final CourseQueryService courseQueryService;
    private final SupervisionPlanService planService;

    @GetMapping("/coverage")
    public SupervisionCoverageResponse coverage(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long departmentId,
            HttpServletRequest request
    ) {
        return courseQueryService.getCoverage(
                semesterId, departmentId, AuthContext.current(request));
    }

    @GetMapping("/plans")
    public SupervisionPlanOverviewResponse plans(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long departmentId,
            HttpServletRequest request
    ) {
        return planService.overview(
                semesterId, departmentId, AuthContext.require(request));
    }

    @PostMapping("/plans")
    public SupervisionPlanItemResponse createPlan(
            @Valid @RequestBody SupervisionPlanRequest body,
            HttpServletRequest request
    ) {
        return planService.create(body, AuthContext.require(request));
    }

    @PatchMapping("/plans/{planId}/status")
    public SupervisionPlanItemResponse updatePlanStatus(
            @PathVariable Long planId,
            @Valid @RequestBody SupervisionPlanStatusRequest body,
            HttpServletRequest request
    ) {
        return planService.updateStatus(
                planId, body.status(), AuthContext.require(request));
    }
}
