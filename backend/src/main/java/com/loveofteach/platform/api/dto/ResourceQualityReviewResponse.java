package com.loveofteach.platform.api.dto;

import com.loveofteach.platform.common.ResourceQualityLevel;

import java.time.LocalDateTime;

public record ResourceQualityReviewResponse(
        Long id,
        Long resourceId,
        ResourceQualityLevel level,
        String levelLabel,
        String comment,
        boolean recommended,
        String reviewerName,
        LocalDateTime reviewedAt
) {
}
