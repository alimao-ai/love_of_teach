package com.loveofteach.platform.domain;

import com.loveofteach.platform.common.BaseEntity;
import com.loveofteach.platform.common.ResourceQualityLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "resource_quality_reviews", uniqueConstraints = @UniqueConstraint(
        name = "uk_resource_review_reviewer", columnNames = {"resource_id", "reviewer_id"}))
public class ResourceQualityReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private TeachingResource resource;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private UserAccount reviewer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ResourceQualityLevel level;

    @Lob
    private String comment;

    @Column(nullable = false)
    private boolean recommended;

    @Column(nullable = false)
    private LocalDateTime reviewedAt;
}
