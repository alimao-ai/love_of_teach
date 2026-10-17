package com.loveofteach.platform.domain;

import com.loveofteach.platform.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_evaluations", uniqueConstraints = @UniqueConstraint(
        name = "uk_evaluation_offering_reviewer", columnNames = {"offering_id", "reviewer_id"}))
public class CourseEvaluation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offering_id", nullable = false)
    private CourseOffering offering;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private UserAccount reviewer;

    @Column(nullable = false)
    private int score;

    @Lob
    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime evaluatedAt;
}
