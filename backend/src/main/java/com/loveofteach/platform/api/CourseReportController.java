package com.loveofteach.platform.api;

import com.loveofteach.platform.api.dto.ReportFileResponse;
import com.loveofteach.platform.security.AuthContext;
import com.loveofteach.platform.service.CourseReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class CourseReportController {

    private final CourseReportService reportService;

    @GetMapping("/offerings/export")
    public ResponseEntity<byte[]> exportOfferings(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long teacherId,
            HttpServletRequest servletRequest
    ) {
        ReportFileResponse report = reportService.exportOfferings(
                AuthContext.require(servletRequest),
                semesterId,
                departmentId,
                teacherId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(report.filename(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .contentType(MediaType.parseMediaType(report.contentType()))
                .body(report.content());
    }
}
