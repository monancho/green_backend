package com.green.university.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.green.university.repository.SubjectJpaRepository;
import com.green.university.repository.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.green.university.dto.CurrentSemesterSubjectSearchFormDto;
import com.green.university.dto.response.SubjectDto;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.service.*;
import com.green.university.utils.StuStatUtil;

/**
 * 수강 신청 관련 REST API Controller (JWT 기반)
 */
@RestController
@RequestMapping("/api/sugang")
public class StuSubController {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SubjectJpaRepository subjectJpaRepository;

    @Autowired
    private CollegeService collegeService;

    @Autowired
    private PreStuSubService preStuSubService;

    @Autowired
    private StuSubService stuSubService;

    @Autowired
    private StuStatService stuStatService;

    @Autowired
    private BreakAppService breakAppService;

    @Autowired
    private UserService userService;

    // 예비 수강신청 기간: 0, 수강신청 기간: 1, 수강신청 기간 종료: 2
    public static int SUGANG_PERIOD = 0;

    /**
     * 과목 조회 (현재 학기)
     */
    @GetMapping("/subjectList/{page}")
    public ResponseEntity<?> readSubjectList(@PathVariable Integer page) {
        List<SubjectDto> subjectList = subjectService.readSubjectListByCurrentSemester();
        int subjectCount = subjectList.size();
        int pageCount = (int) Math.ceil(subjectCount / 20.0);

        // ⭐ 수정: page를 그대로 전달 (offset 계산은 Service에서 함)
        List<SubjectDto> subjectListLimit = subjectService.readSubjectListByCurrentSemesterPage(page);

        List<Department> deptList = collegeService.readDeptAll();
        List<String> subNameList = new ArrayList<>();
        for (SubjectDto subject : subjectList) {
            if (!subNameList.contains(subject.getName())) {
                subNameList.add(subject.getName());
            }
        }
        Map<String, Object> body = new HashMap<>();
        body.put("subjectCount", subjectCount);
        body.put("pageCount", pageCount);
        body.put("page", page);
        body.put("subjectList", subjectListLimit);
        body.put("deptList", deptList);
        body.put("subNameList", subNameList);
        return ResponseEntity.ok(body);
    }


    /**
     * 과목 조회 (현재 학기)에서 필터링
     */
    @GetMapping("/subjectList/search")
    public ResponseEntity<?> readSubjectListSearch(
            @Validated CurrentSemesterSubjectSearchFormDto currentSemesterSubjectSearchFormDto) {

        // ⭐ 디버깅 로그 추가
        System.out.println("검색 요청 받음:");
        System.out.println("type: " + currentSemesterSubjectSearchFormDto.getType());
        System.out.println("deptId: " + currentSemesterSubjectSearchFormDto.getDeptId());
        System.out.println("name: " + currentSemesterSubjectSearchFormDto.getName());

        List<SubjectDto> subjectList = subjectService
                .readSubjectListSearchByCurrentSemester(currentSemesterSubjectSearchFormDto);

        System.out.println("검색 결과 개수: " + subjectList.size());

        int subjectCount = subjectList.size();
        List<Department> deptList = collegeService.readDeptAll();

        List<String> subNameList = new ArrayList<>();
        for (SubjectDto subject : subjectList) {
            if (!subNameList.contains(subject.getName())) {
                subNameList.add(subject.getName());
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("subjectList", subjectList);
        body.put("subjectCount", subjectCount);
        body.put("deptList", deptList);
        body.put("subNameList", subNameList);

        return ResponseEntity.ok(body);
    }

    /**
     * 예비 수강 신청 목록 조회 (페이징)
     */
    @GetMapping("/pre/{page}")
    public ResponseEntity<?> preStuSubApplication(@PathVariable Integer page, Authentication authentication) {
        if (SUGANG_PERIOD != 0) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Integer studentId = Integer.parseInt(authentication.getName());

        Student studentInfo = userService.readStudent(studentId);
        StuStat stuStatEntity = stuStatService.readCurrentStatus(studentInfo.getId());
        List<BreakApp> breakAppList = breakAppService.readByStudentId(studentInfo.getId());
        StuStatUtil.checkStuStat("수강신청", stuStatEntity, breakAppList);

        List<SubjectDto> subjectList = subjectService.readSubjectListByCurrentSemester();
        int subjectCount = subjectList.size();
        int pageCount = (int) Math.ceil(subjectCount / 20.0);

        // ⭐ 수정: page를 그대로 전달
        List<SubjectDto> subjectListLimit = subjectService.readSubjectListByCurrentSemesterPage(page);

        for (SubjectDto sub : subjectListLimit) {
            PreStuSub preStuSub = preStuSubService.readPreStuSub(studentId, sub.getId());
            sub.setStatus(preStuSub != null);
        }

        List<Department> deptList = collegeService.readDeptAll();
        List<String> subNameList = new ArrayList<>();
        for (SubjectDto subject : subjectList) {
            if (!subNameList.contains(subject.getName())) {
                subNameList.add(subject.getName());
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("subjectCount", subjectCount);
        body.put("pageCount", pageCount);
        body.put("page", page);
        body.put("subjectList", subjectListLimit);
        body.put("deptList", deptList);
        body.put("subNameList", subNameList);
        return ResponseEntity.ok(body);
    }

    /**
     * 예비 수강 신청 처리 (신청)
     */
    @PostMapping("/pre/{subjectId}")
    public ResponseEntity<?> insertPreStuSubAppProc(@PathVariable Integer subjectId, Authentication authentication) {
        if (SUGANG_PERIOD != 0) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Integer studentId = Integer.parseInt(authentication.getName());
        preStuSubService.createPreStuSub(studentId, subjectId);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "예비 수강 신청이 완료되었습니다.");
        return ResponseEntity.ok(body);
    }

    /**
     * 예비 수강 신청 처리 (취소)
     */
    @DeleteMapping("/pre/{subjectId}")
    public ResponseEntity<?> deletePreStuSubAppProc(@PathVariable Integer subjectId,
                                                    @RequestParam Integer type, Authentication authentication) {
        if (SUGANG_PERIOD != 0) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Integer studentId = Integer.parseInt(authentication.getName());
        preStuSubService.deletePreStuSub(studentId, subjectId);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "예비 수강 신청이 취소되었습니다.");
        body.put("type", type);
        return ResponseEntity.ok(body);
    }

    /**
     * 예비 수강 신청 강의 목록에서 필터링
     */
    @GetMapping("/pre/search")
    public ResponseEntity<?> preStuSubApplicationSearch(
            @Validated CurrentSemesterSubjectSearchFormDto currentSemesterSubjectSearchFormDto,
            Authentication authentication) {
        if (SUGANG_PERIOD != 0) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Integer studentId = Integer.parseInt(authentication.getName());

        List<SubjectDto> subjectList = subjectService
                .readSubjectListSearchByCurrentSemester(currentSemesterSubjectSearchFormDto);

        for (SubjectDto sub : subjectList) {
            PreStuSub preStuSub = preStuSubService.readPreStuSub(studentId, sub.getId());
            sub.setStatus(preStuSub != null);
        }

        int subjectCount = subjectList.size();
        List<Department> deptList = collegeService.readDeptAll();
        List<String> subNameList = new ArrayList<>();
        for (SubjectDto subject : subjectService.readSubjectListByCurrentSemester()) {
            if (!subNameList.contains(subject.getName())) {
                subNameList.add(subject.getName());
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("subjectCount", subjectCount);
        body.put("subjectList", subjectList);
        body.put("deptList", deptList);
        body.put("subNameList", subNameList);
        return ResponseEntity.ok(body);
    }

    /**
     * 수강 신청 페이지 정보 반환
     */
    @GetMapping("/application/{page}")
    public ResponseEntity<?> stuSubApplication(@PathVariable Integer page, Authentication authentication) {
        if (SUGANG_PERIOD != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Integer studentId = Integer.parseInt(authentication.getName());

        Student studentInfo = userService.readStudent(studentId);
        StuStat stuStatEntity = stuStatService.readCurrentStatus(studentInfo.getId());
        List<BreakApp> breakAppList = breakAppService.readByStudentId(studentInfo.getId());
        StuStatUtil.checkStuStat("수강신청", stuStatEntity, breakAppList);

        List<SubjectDto> subjectList = subjectService.readSubjectListByCurrentSemester();
        int subjectCount = subjectList.size();
        int pageCount = (int) Math.ceil(subjectCount / 20.0);

        // ⭐ 수정: page를 그대로 전달
        List<SubjectDto> subjectListLimit = subjectService.readSubjectListByCurrentSemesterPage(page);

        for (SubjectDto sub : subjectListLimit) {
            StuSub stuSub = stuSubService.readStuSub(studentId, sub.getId());
            sub.setStatus(stuSub != null);
        }

        List<Department> deptList = collegeService.readDeptAll();
        List<String> subNameList = new ArrayList<>();
        for (SubjectDto subject : subjectList) {
            if (!subNameList.contains(subject.getName())) {
                subNameList.add(subject.getName());
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("subjectCount", subjectCount);
        body.put("pageCount", pageCount);
        body.put("page", page);
        body.put("subjectList", subjectListLimit);
        body.put("deptList", deptList);
        body.put("subNameList", subNameList);
        return ResponseEntity.ok(body);
    }

    /**
     * 수강 신청 강의 목록에서 필터링
     */
    @GetMapping("/application/search")
    public ResponseEntity<?> stuSubApplicationSearch(
            @Validated CurrentSemesterSubjectSearchFormDto currentSemesterSubjectSearchFormDto,
            Authentication authentication) {
        if (SUGANG_PERIOD != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Integer studentId = Integer.parseInt(authentication.getName());

        List<SubjectDto> subjectList = subjectService
                .readSubjectListSearchByCurrentSemester(currentSemesterSubjectSearchFormDto);

        for (SubjectDto sub : subjectList) {
            StuSub stuSub = stuSubService.readStuSub(studentId, sub.getId());
            sub.setStatus(stuSub != null);
        }

        int subjectCount = subjectList.size();
        List<Department> deptList = collegeService.readDeptAll();
        List<String> subNameList = new ArrayList<>();
        for (SubjectDto subject : subjectList) {
            if (!subNameList.contains(subject.getName())) {
                subNameList.add(subject.getName());
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("subjectCount", subjectCount);
        body.put("subjectList", subjectList);
        body.put("deptList", deptList);
        body.put("subNameList", subNameList);
        return ResponseEntity.ok(body);
    }

    /**
     * 수강 신청 처리 (신청)
     */
    @PostMapping("/insertApp/{subjectId}")
    public ResponseEntity<?> insertStuSubAppProc(@PathVariable Integer subjectId,
                                                 @RequestParam Integer type, Authentication authentication) {
        if (SUGANG_PERIOD != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Integer studentId = Integer.parseInt(authentication.getName());
        stuSubService.createStuSub(studentId, subjectId);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "수강 신청이 완료되었습니다.");
        body.put("type", type);
        return ResponseEntity.ok(body);
    }

    /**
     * 수강 신청 처리 (취소)
     */
    @DeleteMapping("/deleteApp/{subjectId}")
    public ResponseEntity<?> deleteStuSubAppProc(@PathVariable Integer subjectId,
                                                 @RequestParam Integer type, Authentication authentication) {
        if (SUGANG_PERIOD != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Integer studentId = Integer.parseInt(authentication.getName());
        stuSubService.deleteStuSub(studentId, subjectId);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "수강 신청이 취소되었습니다.");
        body.put("type", type);
        return ResponseEntity.ok(body);
    }

    /**
     * 예비 수강 신청 및 수강 신청 내역 조회
     */
    @GetMapping("/preAppList")
    public ResponseEntity<?> preStuSubAppList(@RequestParam Integer type, Authentication authentication) {
        Integer studentId = Integer.parseInt(authentication.getName());

        // 학적 상태 확인
        Student studentInfo = userService.readStudent(studentId);
        StuStat stuStatEntity = stuStatService.readCurrentStatus(studentInfo.getId());
        List<BreakApp> breakAppList = breakAppService.readByStudentId(studentInfo.getId());
        StuStatUtil.checkStuStat("수강신청", stuStatEntity, breakAppList);

        Map<String, Object> body = new HashMap<>();
        body.put("type", type);

        // 예비 수강 신청 기간에 조회 시
        if (type == 0) {
            List<PreStuSub> preStuSubList = preStuSubService.readPreStuSubList(studentId);

            int sumGrades = preStuSubList.stream()
                    .mapToInt(ps -> {
                        Subject subject = subjectJpaRepository.findById(ps.getSubjectId()).orElse(null);
                        return subject != null ? subject.getGrades() : 0;
                    })
                    .sum();

            body.put("stuSubList", preStuSubList);
            body.put("sumGrades", sumGrades);
            return ResponseEntity.ok(body);
        }

        // 수강 신청 기간에 조회 시
        if (SUGANG_PERIOD != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        List<PreStuSub> preStuSubList1 = stuSubService.readPreStuSubByStuSub(studentId);
        List<StuSub> stuSubList = stuSubService.readStuSubList(studentId);

        int sumGrades = stuSubList.stream()
                .mapToInt(s -> s.getSubject() != null ? s.getSubject().getGrades() : 0)
                .sum();

        body.put("preStuSubList", preStuSubList1);
        body.put("stuSubList", stuSubList);
        body.put("sumGrades", sumGrades);
        return ResponseEntity.ok(body);
    }

    /**
     * 수강 신청 내역 조회
     */
    @GetMapping("/list")
    public ResponseEntity<?> stuSubAppList(Authentication authentication) {
        if (SUGANG_PERIOD == 0) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Integer studentId = Integer.parseInt(authentication.getName());

        // 학적 상태 확인
        Student studentInfo = userService.readStudent(studentId);
        StuStat stuStatEntity = stuStatService.readCurrentStatus(studentInfo.getId());
        List<BreakApp> breakAppList = breakAppService.readByStudentId(studentInfo.getId());
        StuStatUtil.checkStuStat("수강신청", stuStatEntity, breakAppList);

        List<StuSub> stuSubList = stuSubService.readStuSubList(studentId);
        int sumGrades = stuSubList.stream()
                .mapToInt(s -> s.getSubject() != null ? s.getSubject().getGrades() : 0)
                .sum();

        Map<String, Object> body = new HashMap<>();
        body.put("stuSubList", stuSubList);
        body.put("sumGrades", sumGrades);
        return ResponseEntity.ok(body);
    }
}