package com.loveofteach.platform.api.dto;

public record ImportFailureResponse(
        long rowNumber,
        String courseCode,
        String reason
) {
}
