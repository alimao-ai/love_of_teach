package com.loveofteach.platform.api.dto;

import com.loveofteach.platform.common.SupervisionPlanStatus;
import jakarta.validation.constraints.NotNull;

public record SupervisionPlanStatusRequest(
        @NotNull(message = "计划状态不能为空")
        SupervisionPlanStatus status
) {
}
