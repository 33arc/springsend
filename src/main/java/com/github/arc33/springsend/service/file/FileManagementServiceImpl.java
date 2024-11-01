package com.github.arc33.springsend.service.file;

import com.github.arc33.springsend.domain.event.fileoperation.FileOperationEvent;
import com.github.arc33.springsend.domain.event.fileoperation.enums.FileOperationType;
import com.github.arc33.springsend.dto.file.FileDownloadResponse;
import com.github.arc33.springsend.dto.file.FileMetadataDto;
import com.github.arc33.springsend.dto.file.FileUploadMetadata;
import com.github.arc33.springsend.exception.custom.ApiErrorType;
import com.github.arc33.springsend.mapper.FileMetadataMapper;
import com.github.arc33.springsend.model.Category;
import com.github.arc33.springsend.model.FileMetadata;
import com.github.arc33.springsend.repository.fileoperation.FileOperationEventRepository;
import com.github.arc33.springsend.repository.file.metadata.CategoryRepository;
import com.github.arc33.springsend.repository.file.metadata.FileMetadataRepository;
import com.github.arc33.springsend.service.saga.FileUploadSaga;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class FileManagementServiceImpl implements FileManagementService {
    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;
    private final FileMetadataMapper fileMetadataMapper;
    private final CategoryRepository categoryRepository;
    private final FileUploadSaga fileUploadSaga;
    private final FileOperationEventRepository fileOperationEventRepository;

    @Override
    @Transactional
    public FileMetadataDto uploadFile(@NotNull MultipartFile file, @Valid FileUploadMetadata metadata) {
        log.info("Starting file upload process for file: {}", metadata.getFileName());

        try {
            return fileUploadSaga.executeUpload(file, metadata);
        } catch (Exception e) {
            log.error("File upload failed for file: {}", metadata.getFileName(), e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to upload file");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFileOwnedByUser(@NotNull Long fileId, @NotNull String username) {
        validateFileAccess(fileId, username);
        return fileMetadataRepository.existsByIdAndUploader(fileId, username);
    }

    @Override
    @Transactional
    public FileMetadataDto setFileMetadata(@NotNull Long fileId, @Valid FileUploadMetadata metadata) {
        log.info("Updating metadata for file ID: {}", fileId);

        return fileMetadataRepository.findById(fileId)
                .map(existing -> {
                    validateFileAccess(fileId, metadata.getUploader());
                    updateFileMetadata(existing, metadata);
                    FileMetadata updated = fileMetadataRepository.save(existing);
                    recordFileOperation(fileId, FileOperationType.METADATA_UPDATE);
                    return fileMetadataMapper.toDto(updated);
                })
                .orElseThrow(() -> ApiErrorType.RESOURCE_NOT_FOUND.toException("File not found: " + fileId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileMetadataDto> listAllFiles(Pageable pageable) {
        validatePageable(pageable);
        Page<FileMetadata> files = fileMetadataRepository.findAll(pageable);
        return files.map(fileMetadataMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownloadResponse getFileById(@NotNull Long fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() ->ApiErrorType.RESOURCE_NOT_FOUND.toException("File not found: " + fileId));

        try {
            byte[] fileContent = fileStorageService.getFile(metadata.getFileKey());
            recordFileOperation(fileId, FileOperationType.DOWNLOAD);

            return FileDownloadResponse.builder()
                    .content(fileContent)
                    .filename(metadata.getFilename())
//                    .contentType(metadata.getContentType())
                    .size(fileContent.length)
                    .build();
        } catch (Exception e) {
            log.error("Failed to retrieve file content for ID: {}", fileId, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to retrieve file content");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadataDto getFileMetadataById(@NotNull Long fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() ->ApiErrorType.RESOURCE_NOT_FOUND.toException("File not found: " + fileId));

        try {
            return fileMetadataMapper.toDto(metadata);
        } catch (Exception e) {
            log.error("Failed to retrieve file content for ID: {}", fileId, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to retrieve file content");
        }
    }

    @Override
    @Transactional
    public void updateFileContent(@NotNull Long fileId, @NotNull MultipartFile file) {
        log.info("Starting file content update for ID: {}", fileId);

        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> ApiErrorType.RESOURCE_NOT_FOUND.toException("File not found: " + fileId));

        validateFileAccess(fileId, metadata.getUploader());

        try {
            fileStorageService.updateFile(metadata.getFileKey(), file);
            recordFileOperation(fileId, FileOperationType.CONTENT_UPDATE);
            log.info("Successfully updated file content for ID: {}", fileId);
        } catch (Exception e) {
            log.error("Failed to update file content for ID: {}", fileId, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to update file content");
        }
    }

    @Override
    @Transactional
    public void deleteFile(@NotNull Long fileId) {
        log.info("Starting file deletion process for ID: {}", fileId);

        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> ApiErrorType.RESOURCE_NOT_FOUND.toException("File not found: " + fileId));

        try {
            fileStorageService.deleteFile(metadata.getFileKey());
            fileMetadataRepository.deleteById(fileId);
            recordFileOperation(fileId, FileOperationType.DELETE);
            log.info("Successfully deleted file ID: {}", fileId);
        } catch (Exception e) {
            log.error("Failed to delete file ID: {}", fileId, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to delete file content");
        }
    }

    @Override
    @Transactional
    public Category createCategory(@Valid Category category) {
        log.info("Adding new category: {}", category.getName());
        try {
            return categoryRepository.save(category);
        } catch (Exception e) {
            log.error("Failed to add category: {}", category.getName(), e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to add category");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> listAllCategories(Pageable pageable) {
        validatePageable(pageable);
        return categoryRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileMetadataDto> listUserFiles(@NotNull String username, Pageable pageable) {
        validatePageable(pageable);
        return fileMetadataRepository.findAllByAssignedUser(username, pageable)
                .map(fileMetadataMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileMetadataDto> searchFiles(
            String uploader,
            String assignedUser,
            String category,
            String searchTerm,
            int page,
            int size) {
        validatePageable(PageRequest.of(page, size));
        return fileMetadataRepository.searchFiles(uploader, assignedUser, category, searchTerm, page, size);
    }

    private void validateFileAccess(Long fileId, String username) {
        if (!fileMetadataRepository.existsByIdAndUploader(fileId, username)) {
            throw ApiErrorType.NOT_AUTHORIZED.toException("User does not have access to this file");
        }
    }

    private void validatePageable(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
    }

    private void updateFileMetadata(FileMetadata existing, FileUploadMetadata metadata) {
        existing.setFilename(metadata.getFileName());
        existing.setDescription(metadata.getDescription());
        existing.setAssignedUser(metadata.getAssignedUser());
        existing.setExpiryDate(metadata.getExpiryDate());
        Set<Category> categories = metadata.getCategoryIds().stream()
                .map(categoryId -> new Category().setId(categoryId))
                .collect(Collectors.toSet());
        existing.setCategories(categories);
    }

    private void recordFileOperation(Long fileId, FileOperationType operationType) {
        FileOperationEvent event = FileOperationEvent.builder()
                .fileId(fileId)
                .operationType(operationType)
                .timestamp(Instant.now())
                .build();

        fileOperationEventRepository.save(event);
    }
}
