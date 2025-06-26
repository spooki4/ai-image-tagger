package io.github.spooki4.ai_image_tagger.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity  // 데이터베이스 테이블과 매핑되는 JPA 엔티티임을 나타냅니다.
@Getter  // Lombok: 모든 필드의 getter 메소드를 자동으로 생성합니다.
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // Lombok: JPA는 기본 생성자가 필요하므로, 외부에서 함부로 쓰지 못하게 protected로 생성합니다.

public class ImageMetadata {

    @Id  // 이 필드가 테이블의 Primary Key임을 나타냅니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // PK 값을 DB가 자동으로 생성(auto-increment)하도록 합니다.
    private Long id;

    @Column(nullable = false)  // DB 컬럼 설정: null을 허용하지 않습니다.
    private String originalFileName;  // 사용자가 업로드한 원본 파일 이름

    @Column(nullable = false)
    private String savedFileName;  // 서버에 저장될 때의 고유한 파일 이름 (중복 방지)

    @Column(nullable = false)
    private String savedFilePath;  // 서버에 저장된 파일의 전체 경로

    // AI가 분석한 태그들을 콤마(,)로 구분하여 저장할 컬럼입니다.
    @Column(length = 1000)  // 태그가 길어질 수 있으므로 컬럼 길이를 넉넉하게 잡습니다.
    private String tags;

    // AI가 생성한 이미지 설명을 저장할 컬럼입니다.
    @Lob  // 아주 긴 텍스트를 저장할 수 있는 타입(TEXT, CLOB)으로 매핑합니다.
    private String description;

    private LocalDateTime createdAt;  // 생성 일시

    @PrePersist  // JPA 엔티티가 저장되기 직전에 이 메소드가 자동으로 호출됩니다.
    public void createdAt() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder  // 빌더 패턴을 사용하여 객체를 안전하고 편리하게 생성할 수 있게 합니다.
    public ImageMetadata(String originalFileName, String savedFileName, String savedFilePath) {
        this.originalFileName = originalFileName;
        this.savedFileName = savedFileName;
        this.savedFilePath = savedFilePath;
    }
}
