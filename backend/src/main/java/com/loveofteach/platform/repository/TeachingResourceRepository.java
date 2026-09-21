package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.TeachingResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TeachingResourceRepository extends JpaRepository<TeachingResource, Long> {

    List<TeachingResource> findByOfferingIdOrderByUploadedAtDesc(Long offeringId);

    long countByOfferingId(Long offeringId);

    /**
     * 按开课记录批量统计资源数量，避免课程列表逐条查询造成 N+1 问题。
     */
    @Query("""
            select resource.offering.id as offeringId, count(resource) as resourceCount
            from TeachingResource resource
            where resource.offering.id in :offeringIds
            group by resource.offering.id
            """)
    List<OfferingResourceCount> countByOfferingIds(@Param("offeringIds") Collection<Long> offeringIds);

    interface OfferingResourceCount {

        Long getOfferingId();

        Long getResourceCount();
    }
}
