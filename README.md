# Job2Roadmap (JobLoadMap)

데이터 기반 맞춤 IT 학습 가이드 웹서비스의 전체 설계와 초기 구현입니다. 채용 공고(JD)를 입력하면 요구 기술을 분석하고 선수지식부터 실무 스택까지 단계별·주차별 학습 로드맵을 자동 생성합니다.

## 구성

- **backend/**: Spring Boot 기반 REST API 서버
  - JWT 인증과 H2 인메모리 DB 사용
  - JD 텍스트에서 기술 키워드 추출 → 기술 그래프 기반 선수지식 확장 → 단계/주차별 로드맵 생성
  - `/api/auth/*` 회원가입/로그인, `/api/roadmaps/from-jd` 로드맵 생성, `/api/roadmaps` 조회 API 제공
- **frontend/**: HTML/CSS/Vanilla JS 기반 정적 페이지 세트
  - 랜딩, JD 입력, 로드맵 뷰어, 내 로드맵, 로그인, 기술 대시보드, Q&A/챗봇 페이지 포함
  - `frontend/js/api.js` 에서 공통 API 호출 및 토큰 관리

## 실행 방법

### 백엔드

```bash
cd backend
mvn spring-boot:run
```

### 프론트엔드

정적 파일이므로 임의의 정적 서버로 제공하면 됩니다.

```bash
npx serve frontend
```

기본 API 엔드포인트는 `http://localhost:8080`으로 설정되어 있으며 필요 시 `frontend/js/api.js`에서 수정할 수 있습니다.
