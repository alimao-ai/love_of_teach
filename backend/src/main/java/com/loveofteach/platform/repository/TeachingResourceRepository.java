package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.TeachingResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachingResourceRepository extends JpaRepository<TeachingResource, Long> {

    List<TeachingResource> findByOfferingIdOrderByUploadedAtDesc(Long offeringId);

    long countByOfferingId(Long offeringId);
}
