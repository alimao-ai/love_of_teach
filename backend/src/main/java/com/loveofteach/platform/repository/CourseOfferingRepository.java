package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.CourseOffering;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseOfferingRepository extends JpaRepository<CourseOffering, Long> {

    @Query("""
            select o from CourseOffering o
            join fetch o.course c
            join fetch o.teacher t
            join fetch t.department d
            join fetch o.semester s
            where (:semesterId is null or s.id = :semesterId)
              and (:departmentId is null or d.id = :departmentId)
              and (:teacherId is null or t.id = :teacherId)
              and (:keyword is null or lower(c.code) like lower(concat('%', :keyword, '%'))
                   or lower(c.name) like lower(concat('%', :keyword, '%'))
                   or lower(t.name) like lower(concat('%', :keyword, '%')))
            order by s.startDate desc, c.code asc, o.className asc
            """)
    List<CourseOffering> search(
            @Param("semesterId") Long semesterId,
            @Param("departmentId") Long departmentId,
            @Param("teacherId") Long teacherId,
            @Param("keyword") String keyword);

    @Query("""
            select o from CourseOffering o
            join fetch o.course c
            join fetch o.teacher t
            join fetch t.department d
            join fetch o.semester s
            where o.id = :id
            """)
    Optional<CourseOffering> findDetailedById(@Param("id") Long id);

    /**
     * Serializes first-evaluation processing for an offering so the supervision
     * record cannot be inserted twice by concurrent evaluators.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o from CourseOffering o
            join fetch o.course c
            join fetch o.teacher t
            join fetch t.department d
            join fetch o.semester s
            where o.id = :id
            """)
    Optional<CourseOffering> findDetailedByIdForUpdate(@Param("id") Long id);

    Optional<CourseOffering> findByCourseIdAndTeacherIdAndSemesterIdAndClassName(
            Long courseId, Long teacherId, Long semesterId, String className);
}
