package com.loveofteach.platform.api.dto;

import java.util.List;

public record NotificationOverviewResponse(
        long unreadCount,
        List<CourseNotificationResponse> notifications,
        List<CourseSubscriptionResponse> courses
) {
}
