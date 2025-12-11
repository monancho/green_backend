package com.green.university.repository;

import com.green.university.enums.CounselingSlotStatus;
import com.green.university.repository.model.CounselingSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface CounselingSlotJpaRepository extends JpaRepository<CounselingSlot, Long> {
    // 특정 교수의 기간 내 슬롯 조회
    List<CounselingSlot> findByProfessor_IdAndStartAtBetweenOrderByStartAt(
            Integer professorId,
            Timestamp from,
            Timestamp to
    );

    // 특정 교수 + 상태 + 기간
    List<CounselingSlot> findByProfessor_IdAndStatusAndStartAtBetweenOrderByStartAt(
            Integer professorId,
            CounselingSlotStatus status,
            Timestamp from,
            Timestamp to
    );

    // 교수 기준 시간 겹침 여부
    boolean existsByProfessor_IdAndStartAtLessThanAndEndAtGreaterThan(
            Integer professorId,
            Timestamp end,
            Timestamp start
    );

}
