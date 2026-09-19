package com.loveofteach.platform.api.dto;

import java.util.List;

public record ImportResultResponse(
        int totalRows,
        int successCount,
        int failureCount,
        List<ImportFailureResponse> failures,
        String failureCsv
) {
}
