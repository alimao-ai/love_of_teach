package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCodeIgnoreCase(String code);

    boolean existsByDepartmentId(Long departmentId);
}
