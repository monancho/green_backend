package com.green.university.service;

import com.green.university.dto.*;
import com.green.university.dto.response.CounselingReservationResDto;
import com.green.university.dto.response.PrincipalDto;
import com.green.university.enums.CounselingReservationStatus;
import com.green.university.enums.CounselingSlotStatus;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.*;
import com.green.university.repository.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CounselingService {

    @Autowired
    private CounselingSlotJpaRepository slotRepo;

    @Autowired
    private CounselingReservationJpaRepository reservationRepo;

    @Autowired
    private ProfessorJpaRepository professorRepo;

    @Autowired
    private StudentJpaRepository studentRepo;

    @Autowired
    private SubjectJpaRepository subjectRepo;


    // ============= 공통 검증 =============

    // CounselingService 내부

    private boolean isPastSlot(CounselingSlot slot) {
        LocalDateTime now = LocalDateTime.now();
        // 상담이 끝난 시각 기준으로 과거 판단
        LocalDateTime end = slot.getEndAt().toLocalDateTime();
        return end.isBefore(now);
    }


    private void validateStudent(PrincipalDto principal) {
        if (principal == null)
            throw new CustomRestfullException("학생만 이용 가능합니다.", HttpStatus.FORBIDDEN);

        String role = principal.getUserRole();
        if (role == null || !"STUDENT".equals(role.toUpperCase())) {
            throw new CustomRestfullException("학생만 이용 가능합니다.", HttpStatus.FORBIDDEN);
        }
    }

    private void validateProfessor(PrincipalDto principal) {
        if (principal == null)
            throw new CustomRestfullException("교수만 이용 가능합니다.", HttpStatus.FORBIDDEN);

        String role = principal.getUserRole();
        if (role == null || !"PROFESSOR".equals(role.toUpperCase())) {
            throw new CustomRestfullException("교수만 이용 가능합니다.", HttpStatus.FORBIDDEN);
        }
    }

    private void validateSlotOwnerOrAdmin(PrincipalDto principal, CounselingSlot slot) {
        if (principal == null) {
            throw new CustomRestfullException("권한 없음", HttpStatus.FORBIDDEN);
        }

        String role = principal.getUserRole() != null
                ? principal.getUserRole().toUpperCase()
                : "";

        boolean isOwnerProfessor =
                "PROFESSOR".equals(role) &&
                        slot.getProfessor().getId().equals(principal.getId());

        boolean isAdmin = "ADMIN".equals(role);

        if (!isOwnerProfessor && !isAdmin) {
            throw new CustomRestfullException("권한 없음", HttpStatus.FORBIDDEN);
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to, String label) {
        if (from == null || to == null)
            throw new CustomRestfullException(label + " 날짜가 필요합니다.", HttpStatus.BAD_REQUEST);

        if (to.isBefore(from))
            throw new CustomRestfullException(label + " 종료 날짜가 시작 날짜보다 빠릅니다.", HttpStatus.BAD_REQUEST);
    }

    private void validateSlotTime(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null)
            throw new CustomRestfullException("시간이 필요합니다.", HttpStatus.BAD_REQUEST);

        if (!end.isAfter(start))
            throw new CustomRestfullException("종료 시간이 시작 시간보다 빨라야 합니다.", HttpStatus.BAD_REQUEST);

        if (!Duration.between(start, end).equals(Duration.ofHours(1)))
            throw new CustomRestfullException("상담 시간은 1시간만 가능합니다.", HttpStatus.BAD_REQUEST);
    }

    private CounselingSlot findSlot(Long id) {
        return slotRepo.findById(id)
                .orElseThrow(() -> new CustomRestfullException("상담 슬롯 없음", HttpStatus.NOT_FOUND));
    }

    private CounselingReservation findReservation(Long id) {
        return reservationRepo.findById(id)
                .orElseThrow(() -> new CustomRestfullException("예약 없음", HttpStatus.NOT_FOUND));
    }

    private Professor findProfessor(Integer id) {
        return professorRepo.findById(id)
                .orElseThrow(() -> new CustomRestfullException("교수 없음", HttpStatus.NOT_FOUND));
    }

    private Student findStudent(Integer id) {
        return studentRepo.findById(id)
                .orElseThrow(() -> new CustomRestfullException("학생 없음", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void deleteSlot(Long slotId, PrincipalDto principal) {
        CounselingSlot slot = findSlot(slotId);   // 이미 위에 만든 헬퍼
        validateSlotOwnerOrAdmin(principal, slot);

        // 1) 과거 슬롯은 삭제 금지
        if (isPastSlot(slot)) {
            throw new CustomRestfullException(
                    "이미 지난 상담 시간은 수정/삭제할 수 없습니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 2) 예약이 존재하면 삭제 금지 (먼저 예약 취소해야 함)
        boolean hasAnyReservation = reservationRepo.existsBySlot_Id(slotId);
        if (hasAnyReservation) {
            throw new CustomRestfullException(
                    "예약 이력이 있는 시간은 삭제할 수 없습니다. 먼저 예약을 취소하세요.",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 3) 여기까지 오면 그냥 삭제
        slotRepo.delete(slot);
    }
    // ============= DTO 변환 =============

    private CounselingSlotResDto toSlotDto(CounselingSlot slot) {
        CounselingSlotResDto dto = new CounselingSlotResDto();
        dto.setSlotId(slot.getId());
        dto.setProfessorId(slot.getProfessor().getId());
        dto.setProfessorName(slot.getProfessor().getName());
        dto.setStartAt(slot.getStartAt().toLocalDateTime());
        dto.setEndAt(slot.getEndAt().toLocalDateTime());
        dto.setStatus(slot.getStatus());
        dto.setMeetingId(slot.getMeetingId() != null ? slot.getMeetingId() : null);
        return dto;
    }

    private CounselingReservationResDto toReservationDto(CounselingReservation r) {
        CounselingReservationResDto dto = new CounselingReservationResDto();
        dto.setReservationId(r.getId());
        dto.setSlotId(r.getSlot().getId());
        dto.setStudentId(r.getStudent().getId());
        dto.setStudentName(r.getStudent().getName());
        dto.setStatus(r.getStatus());
        dto.setStudentMemo(r.getStudentMemo());
        if (r.getCreatedAt() != null) dto.setCreatedAt(r.getCreatedAt().toLocalDateTime());
        if (r.getCanceledAt() != null) dto.setCanceledAt(r.getCanceledAt().toLocalDateTime());
        return dto;
    }


    // ============= 학생 기능 =============

    @Transactional(readOnly = true)
    public List<Professor> getMyMajorProfessors(PrincipalDto principal) {
        validateStudent(principal);

        Student s = findStudent(principal.getId());
        return professorRepo.findByDepartment_Id(s.getDepartment().getId());
    }

    @Transactional(readOnly = true)
    public List<CounselingSlotResDto> getOpenSlots(Integer professorId, LocalDate from, LocalDate to) {
        validateDateRange(from, to, "슬롯 조회");

        Timestamp fromTs = Timestamp.valueOf(from.atStartOfDay());
        Timestamp toTs = Timestamp.valueOf(to.atTime(LocalTime.MAX));

        return slotRepo.findByProfessor_IdAndStatusAndStartAtBetweenOrderByStartAt(
                        professorId,
                        CounselingSlotStatus.OPEN,
                        fromTs,
                        toTs
                )
                .stream()
                .map(this::toSlotDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CounselingReservationResDto reserveSlot(PrincipalDto principal, Long slotId, String memo) {
        validateStudent(principal);

        CounselingSlot slot = findSlot(slotId);
        if (slot.getStatus() != CounselingSlotStatus.OPEN)
            throw new CustomRestfullException("이미 예약된 슬롯", HttpStatus.BAD_REQUEST);

        Student student = findStudent(principal.getId());

        LocalDateTime start = slot.getStartAt().toLocalDateTime();
        LocalDateTime end = slot.getEndAt().toLocalDateTime();

        boolean overlaps = reservationRepo
                .existsByStudent_IdAndStatusAndSlot_StartAtLessThanAndSlot_EndAtGreaterThan(
                        student.getId(),
                        CounselingReservationStatus.RESERVED,
                        Timestamp.valueOf(end),
                        Timestamp.valueOf(start)
                );

        if (overlaps)
            throw new CustomRestfullException("해당 시간대에 이미 예약 있음", HttpStatus.CONFLICT);

        LocalDateTime now = LocalDateTime.now();

        CounselingReservation r = new CounselingReservation();
        r.setSlot(slot);
        r.setStudent(student);
        r.setStatus(CounselingReservationStatus.RESERVED);
        r.setStudentMemo(memo);
        r.setCreatedAt(Timestamp.valueOf(now));
        r.setUpdatedAt(Timestamp.valueOf(now));

        CounselingReservation saved = reservationRepo.save(r);

        slot.setStatus(CounselingSlotStatus.RESERVED);
        slot.setUpdatedAt(Timestamp.valueOf(now));
        slotRepo.save(slot);

        return toReservationDto(saved);
    }


    @Transactional
    public void cancelReservation(PrincipalDto principal, Long reservationId) {
        validateStudent(principal);

        CounselingReservation r = findReservation(reservationId);
        if (!r.getStudent().getId().equals(principal.getId()))
            throw new CustomRestfullException("본인 예약만 취소 가능", HttpStatus.FORBIDDEN);

        LocalDateTime now = LocalDateTime.now();

        if (r.getSlot().getStartAt().toLocalDateTime().isBefore(now))
            throw new CustomRestfullException("이미 지난 상담은 취소 불가", HttpStatus.BAD_REQUEST);

        r.setStatus(CounselingReservationStatus.CANCELED);
        r.setCanceledAt(Timestamp.valueOf(now));
        r.setUpdatedAt(Timestamp.valueOf(now));
        reservationRepo.save(r);

        // 슬롯을 다시 OPEN 시킬지 체크
        boolean stillReserved = reservationRepo
                .findBySlot_Id(r.getSlot().getId())
                .stream()
                .anyMatch(x -> x.getStatus() == CounselingReservationStatus.RESERVED);

        if (!stillReserved) {
            CounselingSlot s = r.getSlot();
            s.setStatus(CounselingSlotStatus.OPEN);
            s.setUpdatedAt(Timestamp.valueOf(now));
            slotRepo.save(s);
        }
    }


    // ============= 교수 기능 =============

    @Transactional(readOnly = true)
    public List<CounselingSlotResDto> getMySlots(PrincipalDto principal, LocalDate from, LocalDate to) {
        validateProfessor(principal);
        validateDateRange(from, to, "내 슬롯 조회");

        Timestamp fromTs = Timestamp.valueOf(from.atStartOfDay());
        Timestamp toTs = Timestamp.valueOf(to.atTime(LocalTime.MAX));

        return slotRepo.findByProfessor_IdAndStartAtBetweenOrderByStartAt(
                        principal.getId(),
                        fromTs,
                        toTs
                )
                .stream()
                .map(this::toSlotDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public CounselingSlotResDto createSingleSlot(PrincipalDto principal, CreateSingleSlotReqDto dto) {
        validateProfessor(principal);

        validateSlotTime(dto.getStartAt(), dto.getEndAt());

        boolean overlap =
                slotRepo.existsByProfessor_IdAndStartAtLessThanAndEndAtGreaterThan(
                        principal.getId(),
                        Timestamp.valueOf(dto.getEndAt()),
                        Timestamp.valueOf(dto.getStartAt())
                );

        if (overlap)
            throw new CustomRestfullException("이미 다른 상담과 겹칩니다.", HttpStatus.CONFLICT);

        LocalDateTime now = LocalDateTime.now();
        Professor professor = findProfessor(principal.getId());

        CounselingSlot slot = new CounselingSlot();
        slot.setProfessor(professor);
        slot.setStartAt(Timestamp.valueOf(dto.getStartAt()));
        slot.setEndAt(Timestamp.valueOf(dto.getEndAt()));
        slot.setStatus(CounselingSlotStatus.OPEN);
        slot.setCreatedAt(Timestamp.valueOf(now));
        slot.setUpdatedAt(Timestamp.valueOf(now));

        return toSlotDto(slotRepo.save(slot));
    }


    @Transactional
    public List<CounselingSlotResDto> createWeeklyPattern(PrincipalDto principal,
                                                          CreateWeeklyPatternReqDto dto) {
        validateProfessor(principal);

        LocalDate weekStart = dto.getWeekStartDate();
        LocalDate repeatEnd = dto.getRepeatEndDate();
        validateDateRange(weekStart, repeatEnd, "패턴 반복");

        List<CounselingSlotResDto> results = new ArrayList<>();
        Professor professor = findProfessor(principal.getId());
        LocalDateTime now = LocalDateTime.now();

        LocalDate cursor = weekStart;
        while (!cursor.isAfter(repeatEnd)) {

            for (CreateWeeklyPatternReqDto.WeeklyPatternItem item : dto.getItems()) {
                LocalDate targetDate =
                        cursor.with(TemporalAdjusters.nextOrSame(item.getDayOfWeek()));

                if (targetDate.isBefore(cursor) || targetDate.isAfter(cursor.plusDays(6)))
                    continue;

                if (targetDate.isAfter(repeatEnd))
                    continue;

                LocalDateTime start = LocalDateTime.of(targetDate, item.getStartTime());
                LocalDateTime end = LocalDateTime.of(targetDate, item.getEndTime());

                validateSlotTime(start, end);

                boolean overlap =
                        slotRepo.existsByProfessor_IdAndStartAtLessThanAndEndAtGreaterThan(
                                principal.getId(),
                                Timestamp.valueOf(end),
                                Timestamp.valueOf(start)
                        );

                if (overlap) continue;

                CounselingSlot slot = new CounselingSlot();
                slot.setProfessor(professor);
                slot.setStartAt(Timestamp.valueOf(start));
                slot.setEndAt(Timestamp.valueOf(end));
                slot.setStatus(CounselingSlotStatus.OPEN);
                slot.setCreatedAt(Timestamp.valueOf(now));
                slot.setUpdatedAt(Timestamp.valueOf(now));

                results.add(toSlotDto(slotRepo.save(slot)));
            }

            cursor = cursor.plusWeeks(1);
        }

        if (results.isEmpty())
            throw new CustomRestfullException("생성된 슬롯이 없습니다.", HttpStatus.BAD_REQUEST);

        return results;
    }


    @Transactional(readOnly = true)
    public List<CounselingReservationResDto> getSlotReservations(PrincipalDto principal, Long slotId) {

        CounselingSlot slot = findSlot(slotId);

        boolean isOwner = "PROFESSOR".equals(principal.getUserRole())
                && slot.getProfessor().getId().equals(principal.getId());

        boolean isAdmin = "ADMIN".equals(principal.getUserRole());

        if (!isOwner && !isAdmin)
            throw new CustomRestfullException("권한 없음", HttpStatus.FORBIDDEN);

        return reservationRepo.findBySlot_Id(slotId)
                .stream()
                .map(this::toReservationDto)
                .collect(Collectors.toList());
    }
}
