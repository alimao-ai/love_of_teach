package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.ResourceQualityReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ResourceQualityReviewRepository extends JpaRepository<ResourceQualityReview, Long> {

    Optional<ResourceQualityReview> findByResourceIdAndReviewerId(Long resourceId, Long reviewerId);

    Optional<ResourceQualityReview> findFirstByResourceIdOrderByReviewedAtDesc(Long resourceId);

    List<ResourceQualityReview> findByResourceIdInOrderByReviewedAtDesc(Collection<Long> resourceIds);
}
