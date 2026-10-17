package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.ResourceChapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceChapterRepository extends JpaRepository<ResourceChapter, Long> {

    List<ResourceChapter> findByOfferingIdOrderByDisplayOrderAsc(Long offeringId);

    boolean existsByOfferingIdAndTitleIgnoreCase(Long offeringId, String title);

    long countByOfferingId(Long offeringId);
}
