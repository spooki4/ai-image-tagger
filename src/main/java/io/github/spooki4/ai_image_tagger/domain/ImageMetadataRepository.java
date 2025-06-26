package io.github.spooki4.ai_image_tagger.domain;

import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository 상속받는 것만으로 기본적인 CRUD(Save, FindById, Delete 등) 메소드가 자동으로 구현됩니다.
// 첫 번째 제네릭: 어떤 엔티티를 다룰 것인가? (ImageMetadata)
// 두 번째 제네릭: 해당 엔티티의 PK 타입은 무엇인가? (Long)
public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, Long> {
}
