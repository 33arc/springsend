package com.github.arc33.springsend.dto.file;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(chain = true, fluent = true)
public class FileDownloadResponse {
    private String filename;
    private byte[] content;
    private int size;
}
