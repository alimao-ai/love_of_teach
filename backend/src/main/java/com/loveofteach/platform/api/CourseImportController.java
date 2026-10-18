package com.loveofteach.platform.api;

import com.loveofteach.platform.api.dto.ImportResultResponse;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.service.AuthorizationService;
import com.loveofteach.platform.service.CourseImportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/import/courses")
public class CourseImportController {

    private final CourseImportService courseImportService;
    private final AuthorizationService authorizationService;

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("course-import-template.csv", StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok().headers(headers).body(courseImportService.buildTemplate());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResultResponse importCourses(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest servletRequest
    ) {
        authorizationService.requireRole(servletRequest, UserRole.ADMIN);
        return courseImportService.importCourses(file);
    }
}
