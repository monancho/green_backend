package com.green.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.green.university.repository.model.StuStat;

import java.util.List;

/**
 * JPA repository for {@link StuStat} entities.
 */
public interface StuStatJpaRepository extends JpaRepository<StuStat, Integer> {
    List<StuStat> findByStudentIdOrderByIdDesc(Integer studentId);

    StuStat findFirstByStudentIdOrderByIdDesc(Integer studentId);
}