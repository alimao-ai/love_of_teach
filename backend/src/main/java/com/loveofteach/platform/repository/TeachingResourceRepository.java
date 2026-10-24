package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.TeachingResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeachingResourceRepository extends JpaRepository<TeachingResource, Long> {

    List<TeachingResource> findByOfferingIdOrderByUploadedAtDesc(Long offeringId);

    /**
     * Loads resource chapters eagerly for course-detail screens.
     */
    @Query("""
            select resource from TeachingResource resource
            left join fetch resource.chapter
            where resource.offering.id = :offeringId
            order by resource.uploadedAt desc
            """)
    List<TeachingResource> findDetailedByOfferingId(@Param("offeringId") Long offeringId);

    long countByOfferingId(Long offeringId);

    /**
     * 按开课记录批量统计资源数量，避免课程列表逐条查询造成 N+1 问题。
     */
    @Query("""
            select resource.offering.id as offeringId, count(resource) as resourceCount
            from TeachingResource resource
            where resource.offering.id in :offeringIds and resource.active = true
            group by resource.offering.id
            """)
    List<OfferingResourceCount> countByOfferingIds(@Param("offeringIds") Collection<Long> offeringIds);

    @Query("""
            select resource from TeachingResource resource
            join fetch resource.offering offering
            join fetch offering.course course
            join fetch offering.teacher teacher
            join fetch offering.semester semester
            left join fetch resource.chapter chapter
            where (:semesterId is null or semester.id = :semesterId)
              and (:departmentId is null or course.department.id = :departmentId)
              and (:offeringId is null or offering.id = :offeringId)
              and (:chapterId is null or chapter.id = :chapterId)
              and (:resourceType is null or resource.resourceType = :resourceType)
              and resource.active = true
              and (:keyword is null or lower(resource.name) like lower(concat('%', :keyword, '%'))
                   or lower(coalesce(resource.description, '')) like lower(concat('%', :keyword, '%'))
                   or lower(course.name) like lower(concat('%', :keyword, '%')))
            order by resource.uploadedAt desc
            """)
    List<TeachingResource> search(
            @Param("semesterId") Long semesterId,
            @Param("departmentId") Long departmentId,
            @Param("offeringId") Long offeringId,
            @Param("chapterId") Long chapterId,
            @Param("resourceType") com.loveofteach.platform.common.ResourceType resourceType,
            @Param("keyword") String keyword);

    long countByChapterId(Long chapterId);

    @Query("""
            select resource.chapter.id as chapterId, count(resource) as resourceCount
            from TeachingResource resource
            where resource.chapter.id in :chapterIds and resource.active = true
            group by resource.chapter.id
            """)
    List<ChapterResourceCount> countByChapterIds(
            @Param("chapterIds") Collection<Long> chapterIds);

    Optional<TeachingResource> findByStorageKey(String storageKey);

    interface OfferingResourceCount {

        Long getOfferingId();

        Long getResourceCount();
    }

    interface ChapterResourceCount {

        Long getChapterId();

        Long getResourceCount();
    }
}
