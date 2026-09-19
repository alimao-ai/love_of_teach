package com.loveofteach.platform.api.dto;

import com.loveofteach.platform.common.ResourceType;

import java.time.LocalDateTime;

public record CourseResourceResponse(
        Long id,
        String name,
        ResourceType resourceType,
        String resourceTypeLabel,
        String url,
        LocalDateTime uploadedAt
) {
}
