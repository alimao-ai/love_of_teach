package com.loveofteach.platform.api;

import com.loveofteach.platform.api.dto.CourseDrilldownResponse;
import com.loveofteach.platform.security.AuthContext;
import com.loveofteach.platform.service.CourseDrilldownService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drilldown")
public class CourseDrilldownController {

    private final CourseDrilldownService drilldownService;

    @GetMapping
    public CourseDrilldownResponse drilldown(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request
    ) {
        return drilldownService.drilldown(
                semesterId,
                departmentId,
                teacherId,
                keyword,
                AuthContext.require(request));
    }
}
