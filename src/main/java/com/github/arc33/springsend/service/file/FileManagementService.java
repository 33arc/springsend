package com.github.arc33.springsend.service.file;

import com.github.arc33.springsend.dto.file.FileMetadataDto;
import com.github.arc33.springsend.dto.file.FileUploadMetadata;
import com.github.arc33.springsend.model.Category;
import com.github.arc33.springsend.model.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface FileManagementService {
    Optional<FileMetadata> setFileMetadata(Long fileId, FileUploadMetadata metadata);
    FileMetadataDto getFileMetadataById(Long fileId);

    FileMetadata uploadFile(MultipartFile file, FileUploadMetadata metadata);
    PageImpl listAllFiles(Pageable pageable);
    boolean isFileOwnedByUser(Long fileId,String username);
    byte[] getFileContent(Long fileId);
    void updateFileContent(Long fileId, MultipartFile file);
    void deleteFile(Long fileId);
    Page<FileMetadata> listUserFiles(String username, Pageable pageable);
    Page<FileMetadataDto> searchFiles(String uploader, String assignedUser , String category, String searchTerm, int page, int size);
    String getFilename(Long fileId);

    void createCategory(Category category);
    Page<Category> listAllCategories(Pageable pageable);
}