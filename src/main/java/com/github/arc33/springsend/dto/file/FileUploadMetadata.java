package com.github.arc33.springsend.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = false)
public class FileUploadMetadata {
    private String assignedUser;
    private String fileName;
    private String description;
    private String uploader;
    private LocalDateTime expiryDate;
    private Set<Long> categoryIds;
}