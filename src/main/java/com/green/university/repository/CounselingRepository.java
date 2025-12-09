package com.green.university.repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.green.university.repository.model.*;

/**
 * 상담 기록 Repository
 */
@Repository
public interface CounselingRepository extends JpaRepository<Counseling, Integer> {

    // 특정 학생의 모든 상담 기록 조회
    List<Counseling> findByStudentIdOrderByCounselingDateDesc(Integer studentId);

    // 특정 상담자의 상담 기록 조회
    List<Counseling> findByCounselorIdAndCounselorTypeOrderByCounselingDateDesc(
            Integer counselorId, String counselorType);

    // 특정 기간 내 상담 기록 조회
    List<Counseling> findByCounselingDateBetween(Date startDate, Date endDate);

    // 특정 학생의 최근 상담 기록 조회
    Optional<Counseling> findFirstByStudentIdOrderByCounselingDateDesc(Integer studentId);

    // 특정 유형의 상담 기록 조회
    List<Counseling> findByCounselingTypeOrderByCounselingDateDesc(String counselingType);
}