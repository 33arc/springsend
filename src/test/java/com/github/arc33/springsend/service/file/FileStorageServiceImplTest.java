package com.github.arc33.springsend.service.file;

import com.github.arc33.springsend.exception.custom.ApiException;
import com.github.arc33.springsend.repository.file.storage.FileStorageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceImplTest {
    @Mock
    private FileStorageRepository fileStorageRepository;

    @InjectMocks
    private FileStorageServiceImpl fileStorageService;

    @Test
    void storeFile_Success() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "test.txt",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello World".getBytes()
        );

        // Act
        String fileId = fileStorageService.storeFile(file);

        // Assert
        assertNotNull(fileId);
        assertTrue(fileId.contains("test.txt"));
        verify(fileStorageRepository).save(eq(fileId), any(byte[].class));
    }

    @Test
    void storeFile_EmptyFile() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "empty.txt",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class,
                () -> fileStorageService.storeFile(file));
        assertEquals("Failed to store empty file", exception.getErrorResponse().getDescription());
    }

    @Test
    void getFile_Success() throws IOException {
        // Arrange
        String fileId = "test-file-id";
        byte[] expectedData = "Hello World".getBytes();
        when(fileStorageRepository.get(fileId)).thenReturn(expectedData);

        // Act
        byte[] actualData = fileStorageService.getFile(fileId);

        // Assert
        assertArrayEquals(expectedData, actualData);
        verify(fileStorageRepository).get(fileId);
    }

    @Test
    void getFile_NotFound() throws IOException {
        // Arrange
        String fileId = "non-existent-file";
        when(fileStorageRepository.get(fileId)).thenReturn(null);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class,
                () -> fileStorageService.getFile(fileId));
        assertEquals("File not found", exception.getErrorResponse().getDescription());
    }

    @Test
    void deleteFile_Success() throws IOException {
        // Arrange
        String fileId = "test-file-id";
        when(fileStorageRepository.exists(fileId)).thenReturn(true);

        // Act
        boolean result = fileStorageService.deleteFile(fileId);

        // Assert
        assertTrue(result);
        verify(fileStorageRepository).delete(fileId);
    }

    @Test
    void deleteFile_NonExistent() throws IOException {
        // Arrange
        String fileId = "non-existent-file";
        when(fileStorageRepository.exists(fileId)).thenReturn(false);

        // Act
        boolean result = fileStorageService.deleteFile(fileId);

        // Assert
        assertFalse(result);
        verify(fileStorageRepository, never()).delete(fileId);
    }
}