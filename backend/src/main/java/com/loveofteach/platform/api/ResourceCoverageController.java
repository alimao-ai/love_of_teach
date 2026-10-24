package com.loveofteach.platform.api;

import com.loveofteach.platform.api.dto.ResourceCoverageResponse;
import com.loveofteach.platform.security.AuthContext;
import com.loveofteach.platform.service.ResourceCoverageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource-coverage")
public class ResourceCoverageController {

    private final ResourceCoverageService coverageService;

    @GetMapping
    public ResourceCoverageResponse coverage(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long departmentId,
            HttpServletRequest request
    ) {
        return coverageService.coverage(
                semesterId, departmentId, AuthContext.require(request));
    }
}
