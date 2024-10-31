package com.github.arc33.springsend.dto.file;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class FileMetadataDto {
    private Long id;
    private String assignedUser;
    private String filename;
    private String description;
    private String uploader;
    private boolean expires;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiryDate;
    private Set<Long> categoryIds;
}