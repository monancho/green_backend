package com.green.university.service;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.BreakAppJpaRepository;
import com.green.university.repository.StuStatJpaRepository;
import com.green.university.repository.StudentJpaRepository;
import com.green.university.repository.model.BreakApp;
import com.green.university.repository.model.StuStat;
import com.green.university.repository.model.Student;

/**
 * 학적 상태(StuStat) 관련 서비스
 *
 * @author 서영
 */
@Service
public class StuStatService {

    @Autowired
    private StuStatJpaRepository stuStatJpaRepository;

    @Autowired
    private StudentJpaRepository studentJpaRepository;

    @Autowired
    private BreakAppJpaRepository breakAppJpaRepository;

    /**
     * @param studentId
     * @return 해당 학생의 현재 학적 상태 (가장 최근 StuStat)
     */
    @Transactional(readOnly = true)
    public StuStat readCurrentStatus(Integer studentId) {
        // JPA: 특정 학생의 가장 최근 학적 상태 조회
        return stuStatJpaRepository.findFirstByStudentIdOrderByIdDesc(studentId);
    }

    /**
     * @param studentId
     * @return 해당 학생의 전체 학적 변동 내역 (최신순)
     */
    @Transactional(readOnly = true)
    public List<StuStat> readStatusList(Integer studentId) {
        return stuStatJpaRepository.findByStudentIdOrderByIdDesc(studentId);
    }

    /**
     * 모든 학생 id 리스트
     */
    @Transactional(readOnly = true)
    public List<Integer> readIdList() {
        return studentJpaRepository.findAll().stream()
                .map(Student::getId)
                .collect(Collectors.toList());
    }

    /**
     * 처음 학생이 생성될 때 학적 상태 지정 (재학)
     *
     * 첫 학적 상태 저장과 이후 변동 사항을 저장할 때의 메서드를 분리한 이유는
     * 이후 변동 사항을 지정할 때에는 기존의 상태 데이터의 toDate를 현재 날짜로
     * 바꿔주는 작업이 추가로 필요하기 때문임.
     */
    @Transactional
    public void createFirstStatus(Integer studentId) {
        Student student = studentJpaRepository.findById(studentId)
                .orElseThrow(() -> new CustomRestfullException(
                        "학생 정보를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR));

        StuStat stuStat = new StuStat();
        stuStat.setStudent(student);
        stuStat.setStudentId(student.getId());
        stuStat.setStatus("재학");
        // fromDate = 오늘
        stuStat.setFromDate(new Date(System.currentTimeMillis()));
        // toDate = 9999-01-01 (open 상태)
        stuStat.setToDate(Date.valueOf("9999-01-01"));

        stuStatJpaRepository.save(stuStat);
    }

    /**
     * 학적 상태 변동
     *
     * - 기존 학적 상태의 to_date를 now()로 변경
     * - 새로운 학적 상태 레코드 생성
     *
     * @param studentId  대상 학생
     * @param newStatus  새 학적 상태 (예: "휴학", "재학", "졸업" 등)
     * @param newToDate  새 상태의 toDate (예: "9999-01-01"), null 허용
     * @param breakAppId 관련 휴학 신청 id, 없으면 null
     */
    @Transactional
    public void updateStatus(Integer studentId, String newStatus, String newToDate, Integer breakAppId) {
        Student student = studentJpaRepository.findById(studentId)
                .orElseThrow(() -> new CustomRestfullException(
                        "학생 정보를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR));

        // 1) 기존 가장 최근 상태의 toDate를 today로 닫기
        StuStat current = readCurrentStatus(studentId);
        if (current != null) {
            current.setToDate(new Date(System.currentTimeMillis()));
            stuStatJpaRepository.save(current);
        }

        // 2) 새로운 상태 생성
        StuStat newStatusEntity = new StuStat();
        newStatusEntity.setStudent(student);
        newStatusEntity.setStudentId(student.getId());
        newStatusEntity.setStatus(newStatus);
        // fromDate = now
        newStatusEntity.setFromDate(new Date(System.currentTimeMillis()));
        // toDate = 파라미터 값 또는 9999-01-01
        if (newToDate != null) {
            newStatusEntity.setToDate(Date.valueOf(newToDate));
        } else {
            newStatusEntity.setToDate(Date.valueOf("9999-01-01"));
        }

        // 3) 휴학 신청과 연결되는 경우
        if (breakAppId != null) {
            BreakApp breakApp = breakAppJpaRepository.findById(breakAppId)
                    .orElseThrow(() -> new CustomRestfullException(
                            "휴학 신청 정보를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
            newStatusEntity.setBreakApp(breakApp);
        }

        stuStatJpaRepository.save(newStatusEntity);
    }
}
