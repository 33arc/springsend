package com.github.arc33.springsend.domain.event.fileoperation;

import com.github.arc33.springsend.domain.event.fileoperation.enums.UploadStep;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class FileUploadSagaState {
    private String fileKey;
    private Long fileId;
    private UploadStep currentStep;
    private Instant startTime;
    private String error;
}
