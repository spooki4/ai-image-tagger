package io.github.spooki4.ai_image_tagger.service;

import io.github.spooki4.ai_image_tagger.domain.ImageMetadata;
import io.github.spooki4.ai_image_tagger.domain.ImageMetadataRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service  // 이 클래스가 비즈니스 로직을 담당하는 서비스 계층의 컴포넌트임을 나타냅니다.
@RequiredArgsConstructor  // final 또는 @NonNull 필드에 대한 생성자를 자동으로 만들어줍니다. (의존성 주입)
public class ImageUploadService {

    // final 키워드를 사용하여 Repository가 반드시 주입되어야 함을 명시합니다.
    private final ImageMetadataRepository imageMetadataRepository;
    // AI 태거 서비스 의존성 주입
    private final AiTaggerService aiTaggerService;

    // application.properties에 정의한 파일 업로드 경로를 주입받습니다.
    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path uploadPath;  // 경로를 Path 객체로 관리

    @PostConstruct  // 이 서비스 Bean이 초기화된 후 자동으로 이 메소드가 실행됩니다.
    public void init() {
        this.uploadPath = Paths.get(uploadDir);
        try {
            // 폴더가 존재하지 않으면 생성합니다.
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs(); // mkdirs()는 중간 경로가 없어도 모두 생성해줍니다.
                System.out.println("업로드 폴더 생성: " + uploadDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional  // 이 메소드가 하나의 트랜잭션으로 동작하게 합니다. 도중에 예외가 발생하면 모든 DB 작업이 롤백됩니다.
    public ImageMetadata uploadAndAnalyze(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
        }

        // --- 1. AI 분석을 위한 데이터 준비 ---
        byte[] imageBytes = file.getBytes();
        String mimeType = file.getContentType();

        // --- 2. 파일 저장 ---
        String originalFileName = file.getOriginalFilename();
        String savedFileName = createSavedFileName(originalFileName);  // 파일 이름이 중복되지 않도록 UUID를 사용하여 고유한 파일 이름을 생성합니다.
        Path savedPath = this.uploadPath.resolve(savedFileName).toAbsolutePath();  // Path 객체를 사용하여 절대 경로를 구합니다.
        file.transferTo(savedPath.toFile());

        // --- 3. AI 서비스 호출 ---
        String tags = aiTaggerService.generateTags(imageBytes, mimeType);
        String description = aiTaggerService.generateDescription(imageBytes, mimeType);

        // --- 3. 메타데이터 생성 및 저장 ---
        ImageMetadata metadata = ImageMetadata.builder()
            .originalFileName(originalFileName)
            .savedFileName(savedFileName)
            .savedFilePath(savedPath.toString())  // 절대 경로를 저장
            .build();

        // 빌더로 생성된 객체에 AI 결과 설정
        metadata.setTags(tags);
        metadata.setDescription(description);

        return imageMetadataRepository.save(metadata);
    }

    // 원본 파일의 확장자를 유지하면서 새로운 파일 이름을 생성하는 헬퍼 메소드
    private String createSavedFileName(String originalFileName) {
        String ext = extractExt(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    // 확장자(ext)를 추출하는 헬퍼 메소드
    private String extractExt(String originalFileName) {
        int pos = originalFileName.lastIndexOf(".");
        return originalFileName.substring(pos + 1);
    }

    // 모든 이미지 메타데이터를 조회하는 메소드
    @Transactional(readOnly = true)  // 조회만 하므로 readOnly=true 옵션으로 성능 최적화
    public List<ImageMetadata> findAll() {
        return imageMetadataRepository.findAll();
    }
}
