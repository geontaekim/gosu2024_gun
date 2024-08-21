package com.foo.gosucatcher.domain.image.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foo.gosucatcher.domain.expert.domain.Expert;
import com.foo.gosucatcher.domain.item.domain.SubItem;

public interface FileImageRepository extends JpaRepository<FileImage, Long> {
    // userId를 사용하여 FileImage를 조회하는 메서드
    Optional<FileImage> findByUserIdAndType(Long userId, String type);
    
    // 특정 userId로 여러 파일 이미지를 가져올 수 있도록 리스트로 반환
    List<FileImage> findAllByUserIdAndType(Long userId, String type);
    
    // 특정 userId와 fileKey로 파일을 조회
    Optional<FileImage> findByUserIdAndFileKey(Long userId, String fileKey);
    
    // 파일 이름으로 파일을 조회
    Optional<FileImage> findByFileName(String fileName);

	Optional<FileImage> findByUserIdAndFileName(Long expertId, String filename);
}
