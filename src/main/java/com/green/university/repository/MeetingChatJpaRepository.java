package com.green.university.repository;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.green.university.repository.model.MeetingChat;

public interface MeetingChatJpaRepository extends JpaRepository<MeetingChat, Integer> {

    // 최근 메시지들 (Pageable 로 개수/정렬 제어)
    List<MeetingChat> findByMeeting_Id(Integer meetingId, Pageable pageable);

    // afterId 이후 메시지들 (위로 추가 로딩 대신, 재입장/중간 채우기용)
    List<MeetingChat> findByMeeting_IdAndIdGreaterThan(
            Integer meetingId,
            Integer id,
            Pageable pageable
    );

    // beforeId 이전 메시지들 (위로 스크롤 시 과거 로딩)
    List<MeetingChat> findByMeeting_IdAndIdLessThan(
            Integer meetingId,
            Integer id,
            Pageable pageable
    );
}
