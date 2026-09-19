package com.loveofteach.platform.api.dto;

import java.time.LocalDate;

public record SemesterResponse(
        Long id,
        String code,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
}
