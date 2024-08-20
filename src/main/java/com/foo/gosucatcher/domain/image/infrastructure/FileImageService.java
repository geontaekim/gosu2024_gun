package com.foo.gosucatcher.domain.image.infrastructure;

import static com.foo.gosucatcher.global.error.ErrorCode.EMPTY_IMAGE;
import static com.foo.gosucatcher.global.error.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.foo.gosucatcher.global.error.ErrorCode.INVALID_IMAGE_FORMAT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.foo.gosucatcher.domain.image.ImageService;
import com.foo.gosucatcher.domain.image.application.dto.request.ImageUploadRequest;
import com.foo.gosucatcher.domain.image.application.dto.response.ImagesResponse;
import com.foo.gosucatcher.domain.image.domain.FileImage;
import com.foo.gosucatcher.domain.image.domain.FileImageRepository;
import com.foo.gosucatcher.domain.image.exception.ImageIOException;
import com.foo.gosucatcher.domain.image.exception.InvalidFileTypeException;
import com.foo.gosucatcher.global.error.exception.InvalidValueException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FileImageService {

    @Value("${file.upload-dir}") 
    private String uploadDir;
    
    private final FileImageRepository fileImageRepository;

    private static final String[] supportedImageExtension = {"jpg", "jpeg", "png"};

    public ImagesResponse save(ImageUploadRequest request ,Long memberId) {
    	 //기존사진 삭제
    	 deleteExistingImagesByMemberId(memberId);
    	
        List<String> paths = new ArrayList<>();

        // 파일 키 생성 (한 번의 업로드 작업에 대해 동일한 키 사용)
        String fileKey = UUID.randomUUID().toString();
        int seq = 1;

        for (MultipartFile multipartFile : request.files()) {
            validateFile(multipartFile);
            String fullPath = saveFile(multipartFile);

            // 파일 이름만 추출
            String fileName = extractFilename(fullPath);
            // 파일경로만 추출
            String directoryPath = fullPath.substring(0, fullPath.lastIndexOf(File.separator));
            // 파일 정보 DB에 저장
            FileImage fileImage = new FileImage(fileKey, seq, memberId, fileName, directoryPath);
            fileImageRepository.save(fileImage);

            paths.add(directoryPath);
            seq++; // 다음 파일의 seq 값 증가
        }

        return ImagesResponse.from(paths);
    }

    // 파일을 물리 경로에 저장하는 메서드
    private String saveFile(MultipartFile file) {
        try {
            // 업로드 디렉토리가 존재하지 않으면 생성
            Path directoryPath = Paths.get(uploadDir);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // 고유한 파일 이름 생성
            String fileName = getFileName(file.getOriginalFilename());
            Path filePath = directoryPath.resolve(fileName);
            
            Files.createDirectories(filePath.getParent());

            // 파일 저장
            file.transferTo(filePath.toFile());

            // 저장된 파일의 경로를 반환
            return filePath.toString();
        } catch (IOException e) {
            throw new ImageIOException(INTERNAL_SERVER_ERROR);
        }
    }

    // 파일 이름 생성 로직 (고유한 이름을 생성하기 위해 UUID 사용)
    private String getFileName(String originalFilename) {
        StringBuilder fileName = new StringBuilder();

        LocalDateTime now = LocalDateTime.now();
        fileName.append(now.format(DateTimeFormatter.ofPattern("yy/MM/dd/")));

        fileName.append(UUID.randomUUID());

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        fileName.append(".").append(extension);

        return fileName.toString();
    }

    // 파일 유효성 검사 메서드
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidValueException(EMPTY_IMAGE);
        }

        String inputExtension = getExtension(file);
        boolean isExtensionValid = Arrays.stream(supportedImageExtension)
            .anyMatch(extension -> extension.equalsIgnoreCase(inputExtension));

        if (!isExtensionValid) {
            throw new InvalidFileTypeException(INVALID_IMAGE_FORMAT);
        }
    }

    // 파일 확장자 추출 메서드
    private String getExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    

    // 파일 이름 추출 메서드
    private String extractFilename(String fullPath) {
        return new File(fullPath).getName();
    }
    
    
    
    public FileImage getFileImageByMemberId(Long memberId) {
        return fileImageRepository.findByMemberId(memberId)
            .orElseThrow(() -> new EntityNotFoundException("해당 멤버의 파일 이미지를 찾을 수 없습니다."));
    }
    
    // 예: 특정 멤버의 모든 이미지 파일 조회
    public List<FileImage> getAllFileImagesByMemberId(Long memberId) {
        return fileImageRepository.findAllByMemberId(memberId);
    }
    
    
    private void deleteExistingImagesByMemberId(Long memberId) {
        // 1. 기존 FileImage 데이터 조회
        List<FileImage> existingImages = fileImageRepository.findAllByMemberId(memberId);

        // 2. 물리 파일 삭제
        for (FileImage fileImage : existingImages) {
            try {
                Path filePath = Paths.get(fileImage.getFilePath()).resolve(fileImage.getFileName());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new ImageIOException(INTERNAL_SERVER_ERROR);
            }
        }

        // 3. 데이터베이스에서 기존 FileImage 데이터 삭제
        fileImageRepository.deleteAll(existingImages);
    }
    
}
