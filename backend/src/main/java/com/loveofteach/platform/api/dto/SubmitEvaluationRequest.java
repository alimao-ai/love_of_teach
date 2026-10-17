package com.loveofteach.platform.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitEvaluationRequest(
        @NotNull(message = "开课记录不能为空")
        Long offeringId,
        @NotNull(message = "评分不能为空")
        @Min(value = 0, message = "评分不能低于 0")
        @Max(value = 100, message = "评分不能高于 100")
        Integer score,
        @NotBlank(message = "评语不能为空")
        @Size(max = 2000, message = "评语不能超过 2000 个字符")
        String comment
) {
}
