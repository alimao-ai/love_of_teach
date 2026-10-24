package com.loveofteach.platform.service;

import com.loveofteach.platform.api.dto.CourseNotificationResponse;
import com.loveofteach.platform.api.dto.CourseSubscriptionResponse;
import com.loveofteach.platform.api.dto.NotificationOverviewResponse;
import com.loveofteach.platform.common.BusinessException;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.Course;
import com.loveofteach.platform.domain.CourseNotification;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.domain.CourseSubscription;
import com.loveofteach.platform.domain.UserAccount;
import com.loveofteach.platform.repository.CourseNotificationRepository;
import com.loveofteach.platform.repository.CourseOfferingRepository;
import com.loveofteach.platform.repository.CourseRepository;
import com.loveofteach.platform.repository.CourseSubscriptionRepository;
import com.loveofteach.platform.repository.UserAccountRepository;
import com.loveofteach.platform.security.RequestUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理教师课程订阅，并在课程或开课数据变化时生成站内通知。
 */
@Service
@RequiredArgsConstructor
public class CourseNotificationService {

    private final CourseSubscriptionRepository subscriptionRepository;
    private final CourseNotificationRepository notificationRepository;
    private final CourseRepository courseRepository;
    private final CourseOfferingRepository offeringRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public NotificationOverviewResponse overview(RequestUser requestUser) {
        UserAccount teacherUser = requireTeacherUser(requestUser);
        List<CourseOffering> offerings = offeringRepository.search(
                null, null, requestUser.teacherId(), null);
        Map<Long, Course> courses = new LinkedHashMap<>();
        for (CourseOffering offering : offerings) {
            courses.putIfAbsent(offering.getCourse().getId(), offering.getCourse());
        }
        Set<Long> subscribedCourseIds = subscriptionRepository
                .findByTeacherIdAndActiveTrueOrderByCreatedAtDesc(teacherUser.getId())
                .stream()
                .map(subscription -> subscription.getCourse().getId())
                .collect(Collectors.toSet());
        List<CourseSubscriptionResponse> courseItems = courses.values().stream()
                .map(course -> new CourseSubscriptionResponse(
                        course.getId(),
                        course.getCode(),
                        course.getName(),
                        teacherUser.getDisplayName(),
                        subscribedCourseIds.contains(course.getId())))
                .toList();
        List<CourseNotificationResponse> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(teacherUser.getId())
                .stream()
                .map(this::toNotificationResponse)
                .toList();
        return new NotificationOverviewResponse(
                notificationRepository.countByRecipientIdAndReadAtIsNull(teacherUser.getId()),
                notifications,
                courseItems);
    }

    @Transactional
    public CourseSubscriptionResponse subscribe(Long courseId, RequestUser requestUser) {
        UserAccount teacherUser = requireTeacherUser(requestUser);
        Course course = requireTeacherCourse(courseId, requestUser.teacherId());
        CourseSubscription subscription = subscriptionRepository
                .findByTeacherIdAndCourseId(teacherUser.getId(), courseId)
                .orElseGet(() -> CourseSubscription.builder()
                        .teacher(teacherUser)
                        .course(course)
                        .build());
        subscription.setActive(true);
        subscriptionRepository.save(subscription);
        return new CourseSubscriptionResponse(
                course.getId(),
                course.getCode(),
                course.getName(),
                teacherUser.getDisplayName(),
                true);
    }

    @Transactional
    public CourseSubscriptionResponse unsubscribe(Long courseId, RequestUser requestUser) {
        UserAccount teacherUser = requireTeacherUser(requestUser);
        Course course = requireTeacherCourse(courseId, requestUser.teacherId());
        subscriptionRepository.findByTeacherIdAndCourseId(teacherUser.getId(), courseId)
                .ifPresent(subscription -> {
                    subscription.setActive(false);
                    subscriptionRepository.save(subscription);
                });
        return new CourseSubscriptionResponse(
                course.getId(),
                course.getCode(),
                course.getName(),
                teacherUser.getDisplayName(),
                false);
    }

    @Transactional
    public void markAllRead(RequestUser requestUser) {
        UserAccount teacherUser = requireTeacherUser(requestUser);
        notificationRepository.markAllRead(teacherUser.getId(), LocalDateTime.now());
    }

    @Transactional
    public int notifyCourseChanged(Course course, String message) {
        List<CourseSubscription> subscriptions =
                subscriptionRepository.findByCourseIdAndActiveTrue(course.getId());
        for (CourseSubscription subscription : subscriptions) {
            notificationRepository.save(CourseNotification.builder()
                    .recipient(subscription.getTeacher())
                    .course(course)
                    .title("课程信息已更新")
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
        return subscriptions.size();
    }

    private UserAccount requireTeacherUser(RequestUser requestUser) {
        authorizationService.requireAnyRole(requestUser, UserRole.TEACHER);
        if (requestUser.teacherId() == null) {
            throw new BusinessException("教师账号未关联教师档案", HttpStatus.FORBIDDEN);
        }
        return userAccountRepository.findById(requestUser.userId())
                .orElseThrow(() -> new BusinessException("用户不存在", HttpStatus.UNAUTHORIZED));
    }

    private Course requireTeacherCourse(Long courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("课程不存在", HttpStatus.NOT_FOUND));
        if (!offeringRepository.existsByCourseIdAndTeacherId(courseId, teacherId)) {
            throw new BusinessException("只能订阅本人所授课程", HttpStatus.FORBIDDEN);
        }
        return course;
    }

    private CourseNotificationResponse toNotificationResponse(CourseNotification notification) {
        return new CourseNotificationResponse(
                notification.getId(),
                notification.getCourse().getId(),
                notification.getCourse().getCode(),
                notification.getCourse().getName(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getReadAt(),
                notification.getReadAt() != null);
    }
}
