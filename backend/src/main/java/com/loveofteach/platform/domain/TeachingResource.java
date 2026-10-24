package com.loveofteach.platform.domain;

import com.loveofteach.platform.common.BaseEntity;
import com.loveofteach.platform.common.ResourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "teaching_resources")
public class TeachingResource extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offering_id", nullable = false)
    private CourseOffering offering;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ResourceType resourceType;

    @Column(nullable = false, length = 512)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private ResourceChapter chapter;

    @Column(length = 512)
    private String description;

    @Column(length = 128)
    private String originalFilename;

    @Column(length = 128)
    private String contentType;

    private Long fileSize;

    @Column(length = 256)
    private String storageKey;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    private LocalDateTime unavailableAt;

    @Column(length = 512)
    private String unavailableReason;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;
}
