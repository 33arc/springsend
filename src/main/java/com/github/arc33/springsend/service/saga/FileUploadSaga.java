package com.github.arc33.springsend.service.saga;

import com.github.arc33.springsend.domain.event.fileoperation.FileOperationEvent;
import com.github.arc33.springsend.domain.event.fileoperation.FileUploadSagaState;
import com.github.arc33.springsend.domain.event.fileoperation.enums.FileOperationType;
import com.github.arc33.springsend.domain.event.fileoperation.enums.UploadStep;
import com.github.arc33.springsend.dto.file.FileMetadataDto;
import com.github.arc33.springsend.dto.file.FileUploadMetadata;
import com.github.arc33.springsend.exception.custom.ApiErrorType;
import com.github.arc33.springsend.mapper.FileMetadataMapper;
import com.github.arc33.springsend.model.FileMetadata;
import com.github.arc33.springsend.repository.fileoperation.FileOperationEventRepository;
import com.github.arc33.springsend.repository.file.metadata.FileMetadataRepository;
import com.github.arc33.springsend.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadSaga {
    private final FileStorageService fileStorageService;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileMetadataMapper fileMetadataMapper;
    private final FileOperationEventRepository eventRepository;

    @Transactional
    public FileMetadataDto executeUpload(MultipartFile file, FileUploadMetadata metadata) {
        FileUploadSagaState sagaState = createSagaState(file, metadata);

        try {
            // Step 1: Store physical file
            String fileKey = fileStorageService.storeFile(file);
            sagaState.setFileKey(fileKey);
            sagaState.setCurrentStep(UploadStep.FILE_STORED);

            // Step 2: Store metadata
            FileMetadata fileMetadata = fileMetadataMapper.toMetadata(metadata);
            fileMetadata.setFileKey(fileKey);
            FileMetadata saved = fileMetadataRepository.save(fileMetadata);
            sagaState.setFileId(saved.getId());
            sagaState.setCurrentStep(UploadStep.COMPLETED);

            // Record successful upload
            recordFileOperation(saved.getId(), FileOperationType.UPLOAD);

            return fileMetadataMapper.toDto(saved);

        } catch (Exception e) {
            handleSagaFailure(sagaState, e);
            log.info("File upload failed", e);
            throw ApiErrorType.INTERNAL_ERROR.toException("File upload failed");
//            throw new FileOperationException("File upload failed", e);
        }
    }

    private FileUploadSagaState createSagaState(MultipartFile file, FileUploadMetadata metadata) {
        return FileUploadSagaState.builder()
                .currentStep(UploadStep.STARTED)
                .startTime(Instant.now())
                .build();
    }

    private void handleSagaFailure(FileUploadSagaState sagaState, Exception e) {
        log.error("Upload saga failed at step: {}", sagaState.getCurrentStep(), e);

        // Compensating transactions in reverse order
        if (sagaState.getCurrentStep() == UploadStep.COMPLETED ||
                sagaState.getCurrentStep() == UploadStep.METADATA_STORED) {
            compensateMetadataStorage(sagaState);
        }
        if (sagaState.getCurrentStep() == UploadStep.FILE_STORED) {
            compensateFileStorage(sagaState);
        }
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private void compensateFileStorage(FileUploadSagaState sagaState) {
        try {
            fileStorageService.deleteFile(sagaState.getFileKey());
            log.info("Successfully compensated file storage for key: {}", sagaState.getFileKey());
        } catch (Exception e) {
            log.error("Failed to compensate file storage for key: {}", sagaState.getFileKey(), e);
            notifyOperationsTeam(sagaState, e);
        }
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private void compensateMetadataStorage(FileUploadSagaState sagaState) {
        try {
            if (sagaState.getFileId() != null) {
                fileMetadataRepository.deleteById(sagaState.getFileId());
                log.info("Successfully compensated metadata storage for ID: {}", sagaState.getFileId());
            }
        } catch (Exception e) {
            log.error("Failed to compensate metadata storage for ID: {}", sagaState.getFileId(), e);
            notifyOperationsTeam(sagaState, e);
        }
    }

    private void notifyOperationsTeam(FileUploadSagaState sagaState, Exception e) {
        // Implement notification logic (e.g., email, Slack, etc.)
    }

    private void recordFileOperation(Long fileId, FileOperationType operationType) {
        FileOperationEvent event = FileOperationEvent.builder()
                .fileId(fileId)
                .operationType(operationType)
                .timestamp(Instant.now())
                .build();

        eventRepository.save(event);
    }
}