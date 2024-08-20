package com.foo.gosucatcher.domain.image.presentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foo.gosucatcher.domain.image.domain.FileImage;
import com.foo.gosucatcher.domain.image.domain.FileImageRepository;
import com.foo.gosucatcher.global.error.ErrorCode;
import com.foo.gosucatcher.global.error.exception.EntityNotFoundException;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "ImageFileController", description = "이미지파일 화면출력")
@RestController
@RequestMapping("/api/v1/images/image")
@RequiredArgsConstructor
public class ImageFileController {
	
	 private final FileImageRepository fileImageRepository;
	
    @Value("${file.upload-dir}") 
    private String uploadDir;
	

	@GetMapping("/{filename:.+}")
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
		try {
			
			FileImage fileImage = fileImageRepository.findByFileName(filename)
	                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOT_FOUND_IMAGE));
			
			Path file = Paths.get(fileImage.getFilePath()).resolve(filename);
			Resource resource = new UrlResource(file.toUri());

			if (resource.exists() || resource.isReadable()) {
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
						.body(resource);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}
}
