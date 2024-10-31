package com.github.arc33.springsend.repository.file;

import com.github.arc33.springsend.dto.file.FileMetadataDto;
import org.springframework.data.domain.Page;

public interface FileMetadataRepositoryCustom {
    Page<FileMetadataDto> searchFiles(String uploader, String assignedUser, String category, String searchTerm, int page, int size);
}