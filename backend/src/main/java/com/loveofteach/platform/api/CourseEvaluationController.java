package com.loveofteach.platform.api;

import com.loveofteach.platform.api.dto.EvaluationResponse;
import com.loveofteach.platform.api.dto.EvaluationSummaryResponse;
import com.loveofteach.platform.api.dto.SubmitEvaluationRequest;
import com.loveofteach.platform.api.dto.TeacherFeedbackResponse;
import com.loveofteach.platform.security.AuthContext;
import com.loveofteach.platform.service.CourseEvaluationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/evaluations")
public class CourseEvaluationController {

    private final CourseEvaluationService evaluationService;

    @PostMapping
    public EvaluationResponse submit(
            @Valid @RequestBody SubmitEvaluationRequest request,
            HttpServletRequest servletRequest
    ) {
        return evaluationService.submit(request, AuthContext.require(servletRequest));
    }

    @GetMapping("/summary")
    public EvaluationSummaryResponse summary(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long departmentId,
            HttpServletRequest servletRequest
    ) {
        return evaluationService.summary(
                AuthContext.require(servletRequest),
                semesterId,
                departmentId);
    }

    @GetMapping("/mine")
    public TeacherFeedbackResponse myFeedback(
            @RequestParam(required = false) Long semesterId,
            HttpServletRequest servletRequest
    ) {
        return evaluationService.myFeedback(
                AuthContext.require(servletRequest),
                semesterId);
    }
}
