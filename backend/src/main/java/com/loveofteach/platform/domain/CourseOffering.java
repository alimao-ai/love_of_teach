package com.loveofteach.platform.domain;

import com.loveofteach.platform.common.BaseEntity;
import com.loveofteach.platform.common.OfferingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_offerings", uniqueConstraints = @UniqueConstraint(
        name = "uk_offering_course_teacher_semester_class",
        columnNames = {"course_id", "teacher_id", "semester_id", "class_name"}))
public class CourseOffering extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "class_name", nullable = false, length = 64)
    private String className;

    @Column(nullable = false)
    private int studentCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OfferingStatus status;
}
