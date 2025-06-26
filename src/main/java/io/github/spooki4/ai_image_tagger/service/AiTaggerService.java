package io.github.spooki4.ai_image_tagger.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j // 로그를 편하게 찍기 위해 추가
public class AiTaggerService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final String MODEL_NAME = "gemini-1.5-flash-latest"; // 또는 "gemini-pro-vision"

    // 이미지 태깅을 위한 프롬프트
    private static final String PROMPT_FOR_TAGGING =
        "이 이미지에 대한 태그를 생성해줘. " +
        "주요 객체, 배경, 분위기, 색상 등을 고려해서 " +
        "쉼표(,)로 구분된 5~10개의 영어 키워드로만 응답해줘. 다른 설명은 붙이지 마. " +
        "예시: cat, tabby cat, sitting on a couch, warm light, indoor, brown, cozy";

    // 이미지 설명을 위한 프롬프트
    private static final String PROMPT_FOR_DESCRIPTION =
        "이 이미지를 보고 한글로 2~3문장 이내의 간결하고 서정적인 설명을 만들어줘.";

    // 생성자 주입
    public AiTaggerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Gemini API를 호출하여 태그를 생성하는 메소드
    public String generateTags(byte[] imageBytes, String mimeType) throws IOException {
        log.info("Gemini REST API 직접 호출 시작: 태그 생성");
        return callGeminiApi(imageBytes, mimeType, PROMPT_FOR_TAGGING);
    }

    // Gemini API를 호출하여 설명을 생성하는 메소드
    public String generateDescription(byte[] imageBytes, String mimeType) throws IOException {
        log.info("Gemini REST API 직접 호출 시작: 설명 생성");
        return callGeminiApi(imageBytes, mimeType, PROMPT_FOR_DESCRIPTION);
    }

    // Gemini API 호출 공통 로직
    private String callGeminiApi(byte[] imageBytes, String mimeType, String prompt) {
        // 1. API URL 생성
        String apiUrl = String.format(GEMINI_API_URL_TEMPLATE, MODEL_NAME, apiKey);

        // 2. HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. 요청 본문(Body) 생성
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        InlineData inlineData = new InlineData(mimeType, base64Image);
        Part textPart = new Part(prompt, null);
        Part imagePart = new Part(null, inlineData);
        Content content = new Content(Arrays.asList(imagePart, textPart));
        GeminiRequest requestBody = new GeminiRequest(Collections.singletonList(content));

        // 4. HttpEntity로 헤더와 본문 묶기
        HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestBody, headers);

        // 5. REST API 호출
        GeminiResponse response = restTemplate.postForObject(apiUrl, entity, GeminiResponse.class);

        // 6. 응답에서 텍스트 추출 및 반환
        if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
            return response.getCandidates().get(0).getContent().getParts().get(0).getText().trim();
        }

        throw new RuntimeException("Gemini API에서 응답을 받지 못했습니다.");
    }

    // --- Gemini API의 JSON 구조에 맞는 DTO(Data Transfer Object) 클래스들 ---
    // 요청(Request)용 DTO
    @Getter @Setter @AllArgsConstructor
    private static class GeminiRequest {
        private List<Content> contents;
    }
    @Getter @Setter @AllArgsConstructor
    private static class Content {
        private List<Part> parts;
    }
    @Getter @Setter @AllArgsConstructor
    private static class Part {
        private String text;
        @JsonProperty("inline_data")
        private InlineData inlineData;
    }
    @Getter @Setter @AllArgsConstructor
    private static class InlineData {
        @JsonProperty("mime_type")
        private String mimeType;
        private String data;
    }

    // 응답(Response)용 DTO
    @Getter @Setter @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiResponse {
        private List<Candidate> candidates;
    }
    @Getter @Setter @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Candidate {
        private Content content;
    }
}
