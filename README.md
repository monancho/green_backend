# Green University LMS

2025.11 - 2025.12  
총 3명 팀 프로젝트

- URL: https://d3agunwchkh4tm.cloudfront.net/
- 백엔드: https://github.com/wjsgudwnswjsgudwns/GreenUniversity_Backend
- 프론트엔드: https://github.com/wjsgudwnswjsgudwns/Green_University_Frontend

## 1. 프로젝트 개요
---
Green University LMS는 기존 대학 LMS 시스템을 개선해  
학습 관리와 상담 기능을 통합한 차세대 웹 기반 학사지원 플랫폼입니다.  
AI 기능과 실시간 화상 상담을 결합해 학생과 교수 간 소통을 강화하는 것을 목표로 했습니다.

## 2. 팀원 구성
---
| 정현준 | 김윤섭 | 최민수 |
| --- | --- | --- |
| ![github](https://github.com/wjsgudwnswjsgudwns.png) | ![github](https://github.com/monancho.png) | ![github](https://github.com/minsu6862.png) |
| [@wjsgudwnswjsgudwns](https://github.com/wjsgudwnswjsgudwns) | [@monancho](https://github.com/monancho) | [@minsu6862](https://github.com/minsu6862) |

## 3. 기술 환경
---
- Back-end
  - Java 17
  - Spring Boot
  - JPA

- Front-end
  - React

- Database
  - MySQL

- Realtime
  - WebRTC
  - WebSocket

- Infra
  - Docker
  - AWS
  - Caddy

## 4. 기술 채택 이유
---
- WebRTC / WebSocket  
  교수와 학생 간 실시간 화상 상담을 구현하기 위해 선택했습니다.  
  미디어 전송과 화면 상태 동기화를 분리해 안정적인 실시간 통신 구조를 구성했습니다.

- JPA  
  기존 MyBatis 기반 구조를 개선하고, 도메인 중심 설계를 적용하기 위해 선택했습니다.  
  유지보수성과 확장성을 고려한 리팩터링을 진행했습니다.

- HTTPS 기반 배포 환경  
  WebRTC 특성상 HTTPS 환경이 필수였기 때문에,  
  운영 환경과 동일한 조건에서 서비스가 동작하도록 구성했습니다.

## 5. 주요 기능
---
- 학습 관리
  - 학생 및 수강 정보 조회
  - 학습 상태 확인

- 실시간 화상 상담
  - WebRTC 기반 1:1 상담
  - 상담 참여 및 상태 관리

- 상담 예약
  - 상담 일정 등록
  - 신청, 승인, 취소 상태 관리

- 알림 기능
  - 상담 예약 및 상태 변경 알림

## 6. 역할 분담
---
- 정현준 (팀장)  
  프로젝트 전체 구조 설계와 진행을 총괄했다.  
  AI 기반 학습 데이터 분석과 중도이탈 예측 기능을 담당하고, 학사 데이터 통합과 서비스 방향을 관리했다.

- 김윤섭  
  WebRTC 기반 1:1 실시간 화상 상담 기능 구현을 담당했다.  
  WebSocket을 활용한 상담 상태 동기화, 상담 예약 및 알림 CRUD 로직을 설계했으며, 서비스 배포를 전담해 운영 환경을 구성했다.

- 최민수  
  AI 챗봇과 부가 기능 구현을 담당했다.  
  학사 정보 연계 기능과 사용자 보조 기능을 중심으로 개발을 진행했다.

---
본 프로젝트는 중앙정보처리학원 Java 풀스택 개발자 교육 과정의 팀 프로젝트로 진행되었습니다.  
본 프로젝트는 실시간 기능과 서비스 구조 개선 경험을 정리한 기록입니다.
