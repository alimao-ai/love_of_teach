package com.loveofteach.platform.common;

public enum SupervisionPlanStatus {
    PLANNED("待执行"),
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String label;

    SupervisionPlanStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
