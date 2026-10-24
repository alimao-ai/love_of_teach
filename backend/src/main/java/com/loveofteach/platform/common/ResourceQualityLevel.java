package com.loveofteach.platform.common;

public enum ResourceQualityLevel {
    EXCELLENT("优质"),
    QUALIFIED("合格"),
    NEEDS_IMPROVEMENT("待改进");

    private final String label;

    ResourceQualityLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
