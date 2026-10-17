package com.loveofteach.platform.service;

import com.loveofteach.platform.api.dto.CourseResourceResponse;
import com.loveofteach.platform.api.dto.ResourceChapterRequest;
import com.loveofteach.platform.api.dto.ResourceChapterResponse;
import com.loveofteach.platform.api.dto.ResourceSearchItemResponse;
import com.loveofteach.platform.common.BusinessException;
import com.loveofteach.platform.common.ResourceType;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.domain.ResourceChapter;
import com.loveofteach.platform.domain.TeachingResource;
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.ResourceChapterRepository;
import com.loveofteach.platform.repository.TeachingResourceRepository;
import com.loveofteach.platform.security.RequestUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理课程章节、文件资源和资源检索。
 * 上传与章节维护仅允许对应任课教师操作。
 */
@Service
@RequiredArgsConstructor
public class CourseResourceService {

    private final CourseOfferingRepository offeringRepository;
    private final ResourceChapterRepository chapterRepository;
    private final TeachingResourceRepository resourceRepository;
    private final LocalFileStorageService storageService;
    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public List<ResourceChapterResponse> listChapters(Long offeringId) {
        requireOffering(offeringId);
        return chapterRepository.findByOfferingIdOrderByDisplayOrderAsc(offeringId).stream()
                .map(chapter -> toChapterResponse(chapter, resourceRepository.countByChapterId(chapter.getId())))
                .toList();
    }

    @Transactional
    public ResourceChapterResponse createChapter(
            Long offeringId,
            ResourceChapterRequest request,
            RequestUser user
    ) {
        CourseOffering offering = requireOffering(offeringId);
        authorizationService.requireTeacherOwns(user, offering);
        String title = request.title().trim();
        if (chapterRepository.existsByOfferingIdAndTitleIgnoreCase(offeringId, title)) {
            throw new BusinessException("该课程已存在同名章节");
        }
        ResourceChapter chapter = chapterRepository.save(ResourceChapter.builder()
                .offering(offering)
                .title(title)
                .displayOrder(request.displayOrder())
                .description(request.description())
                .build());
        return toChapterResponse(chapter, 0);
    }

    @Transactional
    public CourseResourceResponse uploadResource(
            Long offeringId,
            String name,
            ResourceType resourceType,
            Long chapterId,
            String description,
            MultipartFile file,
            RequestUser user
    ) {
        CourseOffering offering = requireOffering(offeringId);
        authorizationService.requireTeacherOwns(user, offering);
        if (!StringUtils.hasText(name)) {
            throw new BusinessException("资源名称不能为空");
        }
        if (resourceType == null || resourceType == ResourceType.LINK) {
            throw new BusinessException("上传文件时请选择文档、课件或视频类型");
        }
        ResourceChapter chapter = resolveChapter(offeringId, chapterId);
        LocalFileStorageService.StoredFile stored = storageService.store(file);
        TeachingResource resource = resourceRepository.save(TeachingResource.builder()
                .offering(offering)
                .name(name.trim())
                .resourceType(resourceType)
                .url("")
                .chapter(chapter)
                .description(description == null ? null : description.trim())
                .originalFilename(stored.originalFilename())
                .contentType(stored.contentType())
                .fileSize(stored.fileSize())
                .storageKey(stored.storageKey())
                .uploadedAt(LocalDateTime.now())
                .build());
        resource.setUrl("/api/resources/" + resource.getId() + "/download");
        return toResourceResponse(resourceRepository.save(resource));
    }

    @Transactional
    public CourseResourceResponse assignChapter(
            Long resourceId,
            Long chapterId,
            RequestUser user
    ) {
        TeachingResource resource = requireResource(resourceId);
        authorizationService.requireTeacherOwns(user, resource.getOffering());
        resource.setChapter(resolveChapter(resource.getOffering().getId(), chapterId));
        return toResourceResponse(resourceRepository.save(resource));
    }

    @Transactional(readOnly = true)
    public List<ResourceSearchItemResponse> search(
            Long semesterId,
            Long departmentId,
            Long offeringId,
            Long chapterId,
            ResourceType resourceType,
            String keyword
    ) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return resourceRepository.search(
                        semesterId,
                        departmentId,
                        offeringId,
                        chapterId,
                        resourceType,
                        normalizedKeyword)
                .stream()
                .map(this::toSearchItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeachingResource requireResource(Long resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new BusinessException("教学资源不存在", HttpStatus.NOT_FOUND));
    }

    public CourseResourceResponse toResourceResponse(TeachingResource resource) {
        ResourceChapter chapter = resource.getChapter();
        return new CourseResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getResourceType(),
                resourceTypeLabel(resource.getResourceType()),
                resource.getUrl(),
                resource.getUploadedAt(),
                chapter == null ? null : chapter.getId(),
                chapter == null ? null : chapter.getTitle(),
                resource.getDescription(),
                resource.getOriginalFilename(),
                resource.getFileSize(),
                resource.getStorageKey() == null
                        ? resource.getUrl()
                        : "/api/resources/" + resource.getId() + "/download");
    }

    private CourseOffering requireOffering(Long offeringId) {
        return offeringRepository.findDetailedById(offeringId)
                .orElseThrow(() -> new BusinessException("开课记录不存在", HttpStatus.NOT_FOUND));
    }

    private ResourceChapter resolveChapter(Long offeringId, Long chapterId) {
        if (chapterId == null) {
            return null;
        }
        ResourceChapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new BusinessException("章节不存在", HttpStatus.NOT_FOUND));
        if (!chapter.getOffering().getId().equals(offeringId)) {
            throw new BusinessException("章节不属于当前课程");
        }
        return chapter;
    }

    private ResourceChapterResponse toChapterResponse(ResourceChapter chapter, long resourceCount) {
        return new ResourceChapterResponse(
                chapter.getId(),
                chapter.getTitle(),
                chapter.getDisplayOrder(),
                chapter.getDescription(),
                resourceCount);
    }

    private ResourceSearchItemResponse toSearchItem(TeachingResource resource) {
        CourseOffering offering = resource.getOffering();
        ResourceChapter chapter = resource.getChapter();
        return new ResourceSearchItemResponse(
                resource.getId(),
                resource.getName(),
                resource.getResourceType(),
                resourceTypeLabel(resource.getResourceType()),
                resource.getDescription(),
                chapter == null ? null : chapter.getId(),
                chapter == null ? null : chapter.getTitle(),
                offering.getId(),
                offering.getCourse().getCode(),
                offering.getCourse().getName(),
                offering.getTeacher().getName(),
                offering.getSemester().getName(),
                offering.getClassName(),
                resource.getUploadedAt(),
                resource.getStorageKey() == null
                        ? resource.getUrl()
                        : "/api/resources/" + resource.getId() + "/download");
    }

    private String resourceTypeLabel(ResourceType type) {
        return switch (type) {
            case DOCUMENT -> "文档";
            case SLIDES -> "课件";
            case VIDEO -> "视频";
            case LINK -> "链接";
        };
    }
}
