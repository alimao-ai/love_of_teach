package com.loveofteach.platform.repository;

import com.loveofteach.platform.domain.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

    Optional<Semester> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Semester> findAllByOrderByStartDateDesc();

    List<Semester> findByActiveTrueOrderByStartDateDesc();
}
