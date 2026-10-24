package com.loveofteach.platform.api;

import com.loveofteach.platform.api.dto.CourseSubscriptionResponse;
import com.loveofteach.platform.api.dto.NotificationOverviewResponse;
import com.loveofteach.platform.security.AuthContext;
import com.loveofteach.platform.service.CourseNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final CourseNotificationService notificationService;

    @GetMapping
    public NotificationOverviewResponse overview(HttpServletRequest request) {
        return notificationService.overview(AuthContext.require(request));
    }

    @PostMapping("/subscriptions/{courseId}")
    public CourseSubscriptionResponse subscribe(
            @PathVariable Long courseId,
            HttpServletRequest request
    ) {
        return notificationService.subscribe(courseId, AuthContext.require(request));
    }

    @DeleteMapping("/subscriptions/{courseId}")
    public CourseSubscriptionResponse unsubscribe(
            @PathVariable Long courseId,
            HttpServletRequest request
    ) {
        return notificationService.unsubscribe(courseId, AuthContext.require(request));
    }

    @PatchMapping("/read")
    public void markAllRead(HttpServletRequest request) {
        notificationService.markAllRead(AuthContext.require(request));
    }
}
