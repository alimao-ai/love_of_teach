package com.loveofteach.platform.api.dto;

import com.loveofteach.platform.common.ResourceType;

import java.time.LocalDateTime;

public record ResourceSearchItemResponse(
        Long id,
        String name,
        ResourceType resourceType,
        String resourceTypeLabel,
        String description,
        Long chapterId,
        String chapterTitle,
        Long offeringId,
        String courseCode,
        String courseName,
        String teacherName,
        String semesterName,
        String className,
        LocalDateTime uploadedAt,
        String downloadUrl
) {
}
