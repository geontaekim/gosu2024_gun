package com.foo.gosucatcher.domain.image.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class FileImage {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id; // 자동 생성되는 식별자 (내부 관리용)

	    @Column(nullable = false, unique = true)
	    private String fileKey; // 고유한 파일 키

	    @Column(nullable = false)
	    private Integer seq; // 파일의 순서를 나타내는 값

	    @Column(nullable = true) 
	    private Long memberId; 

	    @Column(nullable = false)
	    private String fileName;

	    @Column(nullable = false)
	    private String filePath;

	    public FileImage() {}

	    public FileImage(String fileKey, Integer seq, Long memberId, String fileName, String filePath) {
	        this.fileKey = fileKey;
	        this.seq = seq;
	        this.memberId = memberId;
	        this.fileName = fileName;
	        this.filePath = filePath;
	    }
}
