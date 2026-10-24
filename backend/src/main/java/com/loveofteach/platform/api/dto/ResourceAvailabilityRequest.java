package com.loveofteach.platform.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResourceAvailabilityRequest(
        @NotNull(message = "上下架状态不能为空")
        Boolean active,
        @Size(max = 512, message = "下架原因不能超过 512 个字符")
        String reason
) {
}
