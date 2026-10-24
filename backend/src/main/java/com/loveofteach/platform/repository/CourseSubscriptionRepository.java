package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.CourseSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseSubscriptionRepository extends JpaRepository<CourseSubscription, Long> {

    Optional<CourseSubscription> findByTeacherIdAndCourseId(Long teacherUserId, Long courseId);

    List<CourseSubscription> findByTeacherIdAndActiveTrueOrderByCreatedAtDesc(Long teacherUserId);

    List<CourseSubscription> findByCourseIdAndActiveTrue(Long courseId);
}
