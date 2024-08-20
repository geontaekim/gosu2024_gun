package com.foo.gosucatcher.domain.image.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foo.gosucatcher.domain.expert.domain.Expert;
import com.foo.gosucatcher.domain.item.domain.SubItem;

public interface FileImageRepository extends JpaRepository<FileImage, Long> {
    // 필요하면 추가적인 쿼리 메서드를 정의할 수 있습니다.
	
	// memberId를 사용하여 FileImage를 조회하는 메서드
    Optional<FileImage> findByMemberId(Long memberId);
    
    // 또는 memberId로 여러 파일 이미지를 가져올 수 있도록 리스트로 반환
    List<FileImage> findAllByMemberId(Long memberId);
    
    // 특정 memberId와 fileKey로 파일을 조회
    Optional<FileImage> findByMemberIdAndFileKey(Long memberId, String fileKey);
    
    Optional<FileImage> findByFileName(String fileName);
	
}
