package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.SupervisionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SupervisionRecordRepository extends JpaRepository<SupervisionRecord, Long> {

    long countByOfferingId(Long offeringId);

    boolean existsByOfferingId(Long offeringId);

    List<SupervisionRecord> findByOfferingIdIn(Collection<Long> offeringIds);
}
