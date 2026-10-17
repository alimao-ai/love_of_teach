package com.loveofteach.platform.api.dto;

public record ResourceChapterResponse(
        Long id,
        String title,
        int displayOrder,
        String description,
        long resourceCount
) {
}
