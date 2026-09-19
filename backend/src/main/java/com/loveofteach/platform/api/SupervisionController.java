package com.loveofteach.platform.api;

import com.loveofteach.platform.api.dto.SupervisionCoverageResponse;
import com.loveofteach.platform.service.CourseQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supervision")
public class SupervisionController {

    private final CourseQueryService courseQueryService;

    @GetMapping("/coverage")
    public SupervisionCoverageResponse coverage(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long departmentId
    ) {
        return courseQueryService.getCoverage(semesterId, departmentId);
    }
}
