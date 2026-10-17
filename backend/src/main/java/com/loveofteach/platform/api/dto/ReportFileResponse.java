package com.loveofteach.platform.api.dto;

public record ReportFileResponse(
        String filename,
        String contentType,
        byte[] content
) {
}
