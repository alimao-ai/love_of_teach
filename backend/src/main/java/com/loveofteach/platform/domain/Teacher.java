package com.loveofteach.platform.domain;

import com.loveofteach.platform.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "teachers", uniqueConstraints = @UniqueConstraint(name = "uk_teacher_no", columnNames = "employee_no"))
public class Teacher extends BaseEntity {

    @Column(name = "employee_no", nullable = false, length = 32)
    private String employeeNo;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 64)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}
