package com.loveofteach.platform.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SemesterRequest(
        @NotBlank(message = "学期编码不能为空")
        @Size(max = 32, message = "学期编码不能超过 32 个字符")
        String code,
        @NotBlank(message = "学期名称不能为空")
        @Size(max = 64, message = "学期名称不能超过 64 个字符")
        String name,
        @NotNull(message = "开始日期不能为空")
        LocalDate startDate,
        @NotNull(message = "结束日期不能为空")
        LocalDate endDate,
        @NotNull(message = "启用状态不能为空")
        Boolean active
) {
}
