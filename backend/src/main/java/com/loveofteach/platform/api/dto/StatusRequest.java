package com.loveofteach.platform.api.dto;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(
        @NotNull(message = "启用状态不能为空")
        Boolean active
) {
}
