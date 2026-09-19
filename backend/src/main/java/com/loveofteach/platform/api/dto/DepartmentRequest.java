package com.loveofteach.platform.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "院系编码不能为空")
        @Size(max = 32, message = "院系编码不能超过 32 个字符")
        String code,
        @NotBlank(message = "院系名称不能为空")
        @Size(max = 128, message = "院系名称不能超过 128 个字符")
        String name,
        @NotNull(message = "启用状态不能为空")
        Boolean active
) {
}
