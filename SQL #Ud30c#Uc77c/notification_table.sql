-- 알림 테이블 생성
USE greendb;

CREATE TABLE IF NOT EXISTS notification_tb
(
   id BIGINT PRIMARY KEY AUTO_INCREMENT,
   user_id INT NOT NULL COMMENT '알림을 받는 사용자 ID (교수 또는 학생)',
   type VARCHAR(50) NOT NULL COMMENT '알림 타입: RESERVATION_REQUEST, RESERVATION_APPROVED',
   message VARCHAR(500) NOT NULL COMMENT '알림 메시지',
   is_read TINYINT(1) NOT NULL DEFAULT 0 COMMENT '읽음 여부 (0: 미읽음, 1: 읽음)',
   reservation_id BIGINT COMMENT '관련 예약 ID',
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
   INDEX idx_user_id (user_id),
   INDEX idx_user_id_is_read (user_id, is_read),
   INDEX idx_created_at (created_at)
);

