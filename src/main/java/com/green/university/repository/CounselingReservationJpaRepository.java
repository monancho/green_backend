package com.green.university.repository;

import com.green.university.enums.CounselingReservationStatus;
import com.green.university.repository.model.CounselingReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface CounselingReservationJpaRepository extends JpaRepository<CounselingReservation, Long> {
    // 학생 기준, 기간 내 예약 리스트
    List<CounselingReservation> findByStudent_IdAndSlot_StartAtBetweenOrderBySlot_StartAt(
            Integer studentId,
            Timestamp from,
            Timestamp to
    );

    // 학생 기준, 특정 슬롯을 이미 예약 중인지
    boolean existsBySlot_IdAndStudent_IdAndStatus(
            Long slotId,
            Integer studentId,
            CounselingReservationStatus status
    );

    // 학생 기준, 시간 겹치는 예약이 있는지
    boolean existsByStudent_IdAndStatusAndSlot_StartAtLessThanAndSlot_EndAtGreaterThan(
            Integer studentId,
            CounselingReservationStatus status,
            Timestamp slotEnd,
            Timestamp slotStart
    );
    // 이 slot에 예약이 하나라도 있는지
    boolean existsBySlot_Id(Long slotId);

    // 특정 슬롯의 예약 전체
    List<CounselingReservation> findBySlot_Id(Long slotId);

}
