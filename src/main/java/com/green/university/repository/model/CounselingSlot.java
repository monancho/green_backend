package com.green.university.repository.model;

import com.green.university.enums.CounselingSlotStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "counseling_slot_tb")
public class CounselingSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 교수 (User와 1:1/다:1 구조는 기존 도메인에 맞춰서)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @Column(name = "start_at", nullable = false)
    private Timestamp startAt;   // YYYY-MM-DD HH:00

    @Column(name = "end_at", nullable = false)
    private Timestamp endAt;     // startAt + 1h

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CounselingSlotStatus status;

    // 나중에 WebRTC 회의방과 연결할 때 사용 (nullable)
    @Column(name = "meeting_id")
    private Integer meetingId;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

}
