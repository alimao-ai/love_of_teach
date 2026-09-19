package com.loveofteach.platform.api.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors
) {
}
