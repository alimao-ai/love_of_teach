package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.CourseEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourseEvaluationRepository extends JpaRepository<CourseEvaluation, Long> {

    Optional<CourseEvaluation> findByOfferingIdAndReviewerId(Long offeringId, Long reviewerId);

    List<CourseEvaluation> findByOfferingIdOrderByEvaluatedAtDesc(Long offeringId);

    @Query("""
            select evaluation from CourseEvaluation evaluation
            join fetch evaluation.reviewer
            where evaluation.offering.id in :offeringIds
            order by evaluation.evaluatedAt desc
            """)
    List<CourseEvaluation> findByOfferingIdInWithReviewer(
            @Param("offeringIds") Collection<Long> offeringIds);

    List<CourseEvaluation> findByOfferingIdIn(Collection<Long> offeringIds);

    long countByOfferingId(Long offeringId);
}
