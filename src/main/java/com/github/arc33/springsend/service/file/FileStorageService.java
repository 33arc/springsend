package com.github.arc33.springsend.service.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file);
    void updateFile(String fileKey, MultipartFile file);
    boolean deleteFile(String fileId);
    byte[] getFile(String fileId);
}