package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.SupervisionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupervisionPlanRepository extends JpaRepository<SupervisionPlan, Long> {

    @Query("""
            select plan from SupervisionPlan plan
            join fetch plan.offering offering
            join fetch offering.course course
            join fetch offering.teacher teacher
            join fetch offering.semester semester
            join fetch plan.supervisor supervisor
            where (:semesterId is null or semester.id = :semesterId)
              and (:departmentId is null or course.department.id = :departmentId)
            order by plan.dueDate asc, course.code asc
            """)
    List<SupervisionPlan> search(
            @Param("semesterId") Long semesterId,
            @Param("departmentId") Long departmentId);
}
