package com.loveofteach.platform.common;

import java.util.Locale;

public enum OfferingStatus {
    PLANNED("待开课"),
    OPEN("开课中"),
    CLOSED("已停课");

    private final String label;

    OfferingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static OfferingStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("开课状态不能为空");
        }
        String normalized = value.trim();
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "PLANNED", "待开课", "计划中" -> PLANNED;
            case "OPEN", "开课", "开课中", "已开课" -> OPEN;
            case "CLOSED", "停课", "已停课", "结课" -> CLOSED;
            default -> throw new BusinessException("不支持的开课状态：" + value);
        };
    }
}
