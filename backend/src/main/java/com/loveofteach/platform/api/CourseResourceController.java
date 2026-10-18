package com.loveofteach.platform.api;

import com.loveofteach.platform.api.dto.CourseResourceResponse;
import com.loveofteach.platform.api.dto.ResourceChapterAssignRequest;
import com.loveofteach.platform.api.dto.ResourceChapterRequest;
import com.loveofteach.platform.api.dto.ResourceChapterResponse;
import com.loveofteach.platform.api.dto.ResourceSearchItemResponse;
import com.loveofteach.platform.common.ResourceType;
import com.loveofteach.platform.domain.TeachingResource;
import com.loveofteach.platform.security.AuthContext;
import com.loveofteach.platform.service.CourseResourceService;
import com.loveofteach.platform.service.LocalFileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CourseResourceController {

    private final CourseResourceService resourceService;
    private final LocalFileStorageService storageService;

    @GetMapping("/offerings/{offeringId}/chapters")
    public List<ResourceChapterResponse> listChapters(@PathVariable Long offeringId) {
        return resourceService.listChapters(offeringId);
    }

    @PostMapping("/offerings/{offeringId}/chapters")
    public ResourceChapterResponse createChapter(
            @PathVariable Long offeringId,
            @Valid @RequestBody ResourceChapterRequest request,
            HttpServletRequest servletRequest
    ) {
        return resourceService.createChapter(
                offeringId,
                request,
                AuthContext.require(servletRequest));
    }

    @PostMapping(
            value = "/offerings/{offeringId}/resources",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CourseResourceResponse uploadResource(
            @PathVariable Long offeringId,
            @RequestParam String name,
            @RequestParam ResourceType resourceType,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) String description,
            @RequestParam MultipartFile file,
            HttpServletRequest servletRequest
    ) {
        return resourceService.uploadResource(
                offeringId,
                name,
                resourceType,
                chapterId,
                description,
                file,
                AuthContext.require(servletRequest));
    }

    @PatchMapping("/resources/{resourceId}/chapter")
    public CourseResourceResponse assignChapter(
            @PathVariable Long resourceId,
            @RequestBody ResourceChapterAssignRequest request,
            HttpServletRequest servletRequest
    ) {
        return resourceService.assignChapter(
                resourceId,
                request.chapterId(),
                AuthContext.require(servletRequest));
    }

    @GetMapping("/resources/search")
    public List<ResourceSearchItemResponse> search(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long offeringId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) ResourceType resourceType,
            @RequestParam(required = false) String keyword
    ) {
        return resourceService.search(
                semesterId,
                departmentId,
                offeringId,
                chapterId,
                resourceType,
                keyword);
    }

    @GetMapping("/resources/{resourceId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long resourceId) {
        TeachingResource teachingResource = resourceService.requireResource(resourceId);
        return downloadResource(teachingResource);
    }

    @GetMapping("/resources/files/{storageKey}")
    public ResponseEntity<Resource> downloadByStorageKey(@PathVariable String storageKey) {
        TeachingResource teachingResource =
                resourceService.requireResourceByStorageKey(storageKey);
        return downloadResource(teachingResource);
    }

    private ResponseEntity<Resource> downloadResource(TeachingResource teachingResource) {
        if (teachingResource.getStorageKey() == null) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, teachingResource.getUrl())
                    .build();
        }
        Resource file = storageService.load(teachingResource.getStorageKey());
        MediaType mediaType = teachingResource.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(teachingResource.getContentType());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(teachingResource.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(file);
    }
}
