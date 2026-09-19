package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByEmployeeNoIgnoreCase(String employeeNo);

    List<Teacher> findByIdInOrderByNameAsc(List<Long> ids);

    List<Teacher> findAllByOrderByNameAsc();

    List<Teacher> findByDepartmentIdOrderByNameAsc(Long departmentId);

    long countByDepartmentId(Long departmentId);
}
