package com.loveofteach.platform.common;

public enum UserRole {
    DIRECTOR("教研室主任"),
    SUPERVISOR("教学督导"),
    TEACHER("教师"),
    ADMIN("管理员");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
