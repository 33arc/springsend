package com.github.arc33.springsend.service.file;

import com.github.arc33.springsend.exception.custom.ApiErrorType;
import com.github.arc33.springsend.exception.custom.ApiException;
import com.github.arc33.springsend.repository.file.FileStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "files")
public class FileStorageServiceImpl implements FileStorageService {

    private final FileStorageRepository fileStorageRepository;

    @Override
    @CacheEvict(key = "#result", condition = "#result != null")
    public String storeFile(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw ApiErrorType.INTERNAL_ERROR.toException("Failed to store empty file");
            }

            String fileId = generateFileId(file.getOriginalFilename());
            fileStorageRepository.save(fileId, file.getBytes());

            log.info("Successfully stored file with ID: {}", fileId);
            return fileId;

        } catch(ApiException e) {
            log.error("Failed to store file: {}", file.getOriginalFilename(), e);
            throw e;
        } catch (IOException e) {
            log.error("Failed to store file: {}", file.getOriginalFilename(), e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to store file");
        }
    }

    @Override
    @CacheEvict(key = "#fileKey")
    public void updateFile(String fileKey, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw ApiErrorType.INTERNAL_ERROR.toException("Failed to update with file");
            }

            fileStorageRepository.save(fileKey, file.getBytes());
            log.info("Successfully updated file with key: {}", fileKey);

        } catch (IOException e) {
            log.error("Failed to update file: {}", fileKey, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to update file");
        }
    }

    @Override
    @CacheEvict(key = "#fileId")
    public boolean deleteFile(String fileId) {
        try {
            if (!fileStorageRepository.exists(fileId)) {
                log.warn("Attempted to delete non-existent file: {}", fileId);
                return false;
            }

            fileStorageRepository.delete(fileId);
            log.info("Successfully deleted file: {}", fileId);
            return true;

        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileId, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to delete file");
        }
    }

    @Override
    @Cacheable(key = "#fileId", unless = "#result == null")
    public byte[] getFile(String fileId) {
        try {
            byte[] fileData = fileStorageRepository.get(fileId);
            if (fileData == null) {
                log.warn("File not found: {}", fileId);
                throw ApiErrorType.RESOURCE_NOT_FOUND.toException("File not found");
            }

            log.debug("Retrieved file: {}", fileId);
            return fileData;

        } catch (IOException e) {
            log.error("Failed to retrieve file: {}", fileId, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to retrieve file");
        }
    }

    private String generateFileId(String originalFilename) {
        return UUID.randomUUID().toString() + "_" + sanitizeFilename(originalFilename);
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}