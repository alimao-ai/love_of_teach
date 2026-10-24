package com.loveofteach.platform.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SupervisionPlanRequest(
        @NotNull(message = "开课记录不能为空")
        Long offeringId,
        @NotNull(message = "计划日期不能为空")
        LocalDate plannedDate,
        @NotNull(message = "截止日期不能为空")
        LocalDate dueDate,
        @Size(max = 512, message = "计划说明不能超过 512 个字符")
        String note
) {
}
