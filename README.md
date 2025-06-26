# 🖼️ AI 이미지 태거 & 설명 생성기

**Java(Spring Boot)와 Google Gemini API를 활용하여, 업로드된 이미지의 태그와 서정적인 설명을 자동으로 생성하는 간단한 웹 애플리케이션입니다.**

이 프로젝트는 강력한 생성형 AI 모델을 웹 서비스에 통합하는 방법을 보여줍니다. 사용자가 이미지를 업로드하면, 애플리케이션은 Gemini API를 호출하여 이미지에 대한 통찰력 있는 메타데이터(태그, 설명)를 얻어와 화면에 표시하고 데이터베이스에 저장합니다.

## ✨ 주요 기능

-   **이미지 업로드**: 직관적인 웹 인터페이스를 통한 이미지 파일 업로드 기능
-   **AI 태그 생성**: 각 이미지에 대해 연관성 높은 5~10개의 영어 키워드를 자동으로 생성합니다.
-   **AI 설명 생성**: 이미지의 분위기를 담은 짧고 서정적인 한글 설명을 자동으로 생성합니다.
-   **메타데이터 저장**: 이미지 정보(원본 파일명, 저장 경로, 태그, 설명)를 인메모리 H2 데이터베이스에 저장합니다.
-   **동적 갤러리**: 업로드되고 분석된 모든 이미지들을 갤러리 형태로 보여줍니다.

## 🛠️ 기술 스택

-   **백엔드**: Java 17, Spring Boot 3.x
    -   `Spring Web`: 웹 애플리케이션 및 REST API 구축
    -   `Spring Data JPA`: 데이터베이스 연동 및 관리
    -   `Thymeleaf`: 서버 사이드 렌더링을 위한 템플릿 엔진
    -   `Lombok`: 반복적인 코드(Getter, Setter 등) 작성을 줄여주는 라이브러리
-   **데이터베이스**: H2 In-Memory Database
-   **AI 모델**: Google Gemini 1.5 Flash (REST API 직접 호출 방식)
-   **빌드 도구**: Gradle

## 🚀 시작하기

로컬 환경에서 개발 및 테스트를 위해 프로젝트를 설정하고 실행하는 방법입니다.

### 사전 준비물

-   Java (JDK) 17 이상
-   Gradle 8.x 버전
-   IntelliJ IDEA 또는 Eclipse와 같은 IDE
-   Google AI Studio에서 발급받은 **[Google Gemini API 키](https://aistudio.google.com/app/apikey)**

### 설치 및 설정 방법

1.  **리포지토리 클론:**
    터미널을 열고 아래 명령어를 실행하여 프로젝트를 클론합니다.
    ```bash
    git clone https://github.com/your-username/ai-image-tagger.git
    cd ai-image-tagger
    ```

2.  **설정 파일 생성 및 API 키 입력 (가장 중요!)**
    민감한 정보인 API 키를 안전하게 관리하기 위해, 설정 파일은 Git 추적에서 제외됩니다. 예제 파일을 복사하여 실제 설정 파일을 직접 생성해야 합니다.

    -   `src/main/resources/` 디렉토리로 이동합니다.
    -   `application-example.properties` 파일을 복사하여 `application.properties` 라는 이름으로 새 파일을 만듭니다.
    -   새로 생성한 `application.properties` 파일을 열고, 아래와 같이 본인의 실제 Gemini API 키를 입력합니다.

    ```properties
    # application.properties

    # ... 다른 설정들 ...

    # Google Gemini API 설정
    gemini.api.key=AIzaSy...여기에_발급받은_API_키를_붙여넣으세요
    ```

3.  **프로젝트 빌드:**
    IDE에서 프로젝트를 열면 자동으로 빌드되거나, 터미널에서 아래 명령어로 수동 빌드가 가능합니다.
    ```bash
    ./gradlew build
    ```

4.  **애플리케이션 실행:**
    IDE의 실행 버튼을 누르거나, 터미널에서 아래 명령어를 입력하여 애플리케이션을 실행합니다.
    ```bash
    ./gradlew bootRun
    ```

5.  **애플리케이션 접속:**
    애플리케이션 실행이 완료되면, 웹 브라우저를 열고 아래 주소로 접속합니다.
    [http://localhost:8080](http://localhost:8080)

## 🔧 동작 원리

1.  **이미지 업로드**: 사용자가 웹 UI에서 이미지 파일을 선택하고 폼을 제출하면, `ImageUploadController`가 `MultipartFile`을 수신합니다.
2.  **서비스 로직 처리**: 컨트롤러는 수신한 파일을 `ImageUploadService`로 전달합니다.
3.  **파일 저장**: 서비스는 업로드된 파일을 서버의 특정 디렉토리(`uploads/`)에 고유한 UUID 파일명으로 저장하여 이름 중복을 방지합니다.
4.  **AI 분석**:
    -   `ImageUploadService`는 `AiTaggerService`를 호출합니다.
    -   `AiTaggerService`는 Spring의 `RestTemplate`을 사용하여 Google Gemini REST API에 직접 POST 요청을 보냅니다.
    -   요청 시 이미지 데이터(Base64 인코딩 문자열)와 특정 프롬프트(태그용, 설명용)를 함께 전송합니다.
5.  **데이터베이스 저장**: 원본 파일명, 저장된 파일명, 파일 경로, 그리고 AI가 생성한 태그와 설명이 `ImageMetadata` 엔티티로 H2 데이터베이스에 저장됩니다.
6.  **결과 표시**: 페이지가 새로고침되면 `ImageUploadController`가 데이터베이스의 모든 `ImageMetadata`를 조회하여, 새롭게 분석된 이미지를 포함한 전체 갤러리를 화면에 렌더링합니다.
