package com.loveofteach.platform.service;

import com.loveofteach.platform.api.dto.CourseResourceResponse;
import com.loveofteach.platform.api.dto.ResourceQualityReviewRequest;
import com.loveofteach.platform.api.dto.ResourceQualityReviewResponse;
import com.loveofteach.platform.common.BusinessException;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.ResourceQualityReview;
import com.loveofteach.platform.domain.TeachingResource;
import com.loveofteach.platform.domain.UserAccount;
import com.loveofteach.platform.repository.ResourceQualityReviewRepository;
import com.loveofteach.platform.repository.UserAccountRepository;
import com.loveofteach.platform.security.RequestUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 督导/主任对教学资源进行质量评级和优质推荐。
 */
@Service
@RequiredArgsConstructor
public class ResourceQualityService {

    private final ResourceQualityReviewRepository reviewRepository;
    private final UserAccountRepository userAccountRepository;
    private final CourseResourceService resourceService;
    private final AuthorizationService authorizationService;

    @Transactional
    public ResourceQualityReviewResponse review(
            Long resourceId,
            ResourceQualityReviewRequest request,
            RequestUser requestUser
    ) {
        authorizationService.requireAnyRole(
                requestUser, UserRole.SUPERVISOR, UserRole.DIRECTOR, UserRole.ADMIN);
        TeachingResource resource = resourceService.requireResource(resourceId);
        UserAccount reviewer = userAccountRepository.findById(requestUser.userId())
                .orElseThrow(() -> new BusinessException("用户不存在", HttpStatus.UNAUTHORIZED));
        ResourceQualityReview review = reviewRepository
                .findByResourceIdAndReviewerId(resourceId, reviewer.getId())
                .orElseGet(() -> ResourceQualityReview.builder()
                        .resource(resource)
                        .reviewer(reviewer)
                        .build());
        review.setLevel(request.level());
        review.setComment(request.comment());
        review.setRecommended(request.recommended());
        review.setReviewedAt(LocalDateTime.now());
        ResourceQualityReview saved = reviewRepository.save(review);
        return new ResourceQualityReviewResponse(
                saved.getId(),
                resourceId,
                saved.getLevel(),
                saved.getLevel().getLabel(),
                saved.getComment(),
                saved.isRecommended(),
                reviewer.getDisplayName(),
                saved.getReviewedAt());
    }

    @Transactional(readOnly = true)
    public CourseResourceResponse resource(Long resourceId) {
        return resourceService.toResourceResponse(resourceService.requireResource(resourceId));
    }
}
