package com.loveofteach.platform.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResourceChapterRequest(
        @NotBlank(message = "章节名称不能为空")
        @Size(max = 128, message = "章节名称不能超过 128 个字符")
        String title,
        @Min(value = 1, message = "章节顺序必须大于 0")
        int displayOrder,
        @Size(max = 512, message = "章节说明不能超过 512 个字符")
        String description
) {
}
