# JobRoadMap (JRM)

데이터 기반 맞춤 IT 학습 가이드 웹서비스의 프로토타입입니다. 채용 공고(JD) URL/텍스트를 입력하면 선수지식 → 핵심기술 → 실무스택 → 보너스스킬 구조로 재정렬된 로드맵을 생성하고, 하루 학습 시간과 기간을 반영해 주차별 학습 플랜을 제공합니다. 또한 매일 0시에 실행되는 간단한 크롤러가 샘플 공고를 파싱해 기술 키워드 데이터를 준비합니다.

## 구성
- **backend/**: Java 17 + Spring Boot 3.3 기반 REST API
  - `/api/roadmap/from-jd` : JD 텍스트/URL 기반 로드맵 생성 (Jsoup 파싱)
  - `/api/crawl/daily` : 샘플 HTML을 크롤링해 키워드/공고 목록 제공
  - `/api/auth`, `/api/profile`, `/api/attendance`, `/api/community` : 데모용 인증/저장/출석/커뮤니티
- **frontend/**: HTML/CSS/Vanilla JS 기반 멀티 페이지
  - `index.html`: 화이트/블루 톤 랜딩 페이지
  - `builder.html`: JD URL/텍스트 입력, 로드맵 생성/저장
  - `my-roadmaps.html`: 로그인 후 로드맵 목록, 진행률/출석 관리
  - `community.html`: 정보 공유/게시글 작성

## 실행 방법
1. **백엔드 실행**
   - Java 17과 Maven이 설치되어 있어야 합니다.
   - 터미널에서 백엔드 디렉터리로 이동 후 실행:
     ```bash
     cd backend
     mvn spring-boot:run
     ```
   - 서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.

2. **프론트엔드 실행**
   - 정적 파일 기반이므로 별도 빌드 과정이 없습니다.
   - 새 터미널에서 프로젝트 루트(frontend 상위)에서 간단히 개발용 서버를 띄웁니다.
     ```bash
     cd frontend
     python -m http.server 8000
     ```
   - 브라우저에서 `http://localhost:8000`에 접속합니다.

3. **AI API 키 연결 (옵션)**
   - 외부 AI API를 붙여 실습 미션을 생성하려면 아래처럼 환경 변수를 주입하거나 `backend/src/main/resources/application.properties`에 값을 설정합니다.
     ```bash
     export AI_API_URL="https://api.your-llm.com/generate"
     export AI_API_KEY="YOUR_KEY"
     ```
   - `AiAdapter`가 `topics` 배열을 `missions` 배열로 돌려주는 JSON 응답을 기대하며, 키를 설정하지 않으면 내부 기본 미션 문구로 대체합니다.

4. **사용 흐름**
   - `frontend`를 띄운 뒤 **로드맵 생성** 페이지에서 URL 또는 텍스트를 입력하고 준비 기간/레벨을 고른 뒤 **로드맵 생성하기**를 누릅니다.
   - JD URL이 제공되면 Jsoup으로 본문을 크롤링하고, 추출 키워드를 기반으로 `AiAdapter`가 외부 AI API를 호출합니다(키를 넣지 않으면 기본 요약으로 대체).
   - 로그인(샘플 토큰 발급) 후 **저장** 버튼으로 내 로드맵을 저장하고, **내 로드맵** 페이지에서 진행률을 확인합니다
- **정보공유** 페이지에서 게시글을 작성하거나 기존 글을 확인해 자료를 공유할 수 있습니다.

## 참고
- 크롤링은 `sample-posts.html`을 파싱하는 형태로 구현되어 있어 네트워크 없이도 동작합니다. 실제 서비스로 확장 시 Jsoup 기반으로 실서비스 사이트를 파싱하도록 교체하면 됩니다.
- 로드맵 미션은 `AiAdapter`를 통해 외부 AI API 호출 지점을 분리해 두었으므로 API 키만 주입하면 확장 가능합니다.

## Render 배포 가이드
`render.yaml` 블루프린트로 백엔드(Web Service)와 프론트엔드(Static Site)를 한 번에 생성할 수 있습니다.

1. 이 저장소를 GitHub에 올린 뒤 Render에서 **New > Blueprint**로 연결합니다. 루트의 `render.yaml`이 자동 감지됩니다.
2. **백엔드(jrm-backend)**  
   - Build: `./.maven/apache-maven-3.9.9/bin/mvn -B -DskipTests package`  
   - Start: `java -Dserver.port=${PORT} -jar target/job-roadmap-0.0.1-SNAPSHOT.jar`  
   - 필수/선택 환경변수(Deploy 탭에서 입력):
     - `AI_API_URL` 예시: `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent`
     - `AI_API_KEY` (Render에서 Secret으로 입력)
     - `SPRING_DATASOURCE_URL` 기본은 H2 파일 DB(`jdbc:h2:file:./data/jrmdb_v2;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE`, 배포 시 휘발성). 영속 DB가 필요하면 Render PostgreSQL을 만든 뒤 JDBC URL/계정으로 교체.
     - `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` (외부 DB 사용 시)
     - `SPRING_JPA_HIBERNATE_DDL_AUTO` 기본 `update`
     - `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS` (SMTP 필요 시)
3. **프론트엔드(jrm-frontend)**  
   - `rootDir: frontend`, `publishPath: .`  
   - Build 단계에서 `API_BASE_URL`을 넣으면 `api-config.js`를 해당 주소로 덮어씁니다. 값이 없으면 로컬 기본(`http://localhost:8080`)을 사용합니다.
   - `API_BASE_URL`에 백엔드 도메인(예: `https://jrm-backend.onrender.com`)을 설정하세요.
4. 배포 후 확인
   - 백엔드: `GET {BACKEND_URL}/api/health/ai` (AI 키가 없으면 `aiConnected: false` 정상)
   - 프론트엔드: Render가 제공한 도메인으로 접속 후 로드맵 생성/로그인 등 기능 확인
