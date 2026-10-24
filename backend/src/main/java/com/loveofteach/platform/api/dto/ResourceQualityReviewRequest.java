package com.loveofteach.platform.api.dto;

import com.loveofteach.platform.common.ResourceQualityLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResourceQualityReviewRequest(
        @NotNull(message = "质量等级不能为空")
        ResourceQualityLevel level,
        @Size(max = 1000, message = "评审意见不能超过 1000 个字符")
        String comment,
        boolean recommended
) {
}
