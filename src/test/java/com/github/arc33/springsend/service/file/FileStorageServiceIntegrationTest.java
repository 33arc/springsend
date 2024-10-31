package com.github.arc33.springsend.service.file;

import com.github.arc33.springsend.BaseIntegrationTest;
import com.github.arc33.springsend.SpringsendApplication;
import com.github.arc33.springsend.exception.custom.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.junit.Assert.*;

// Integration Tests
@SpringBootTest(classes = {
        SpringsendApplication.class,
        BaseIntegrationTest.CommonTestConfig.class
},properties = {
        "user-management-service.max-page-size=100"
})
@Profile("test")
@Testcontainers
class FileStorageServiceIntegrationTest extends BaseIntegrationTest {
    @Container
    static GenericContainer<?> etcd = new GenericContainer<>("quay.io/coreos/etcd:v3.5.0")
            .withExposedPorts(2379)
            .withCommand(
                    "/usr/local/bin/etcd",
                    "--advertise-client-urls=http://0.0.0.0:2379",
                    "--listen-client-urls=http://0.0.0.0:2379"
            );

    @DynamicPropertySource
    static void registerEtcdProperties(DynamicPropertyRegistry registry) {
        registry.add("etcd.endpoints", () ->
                String.format("http://%s:%d", etcd.getHost(), etcd.getMappedPort(2379)));
    }

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    void fullLifecycle() throws IOException {
        // Store file
        MultipartFile file = new MockMultipartFile(
                "test.txt",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Integration Test Content".getBytes()
        );
        String fileId = fileStorageService.storeFile(file);
        assertNotNull(fileId);

        // Get file
        byte[] retrievedData = fileStorageService.getFile(fileId);
        assertArrayEquals("Integration Test Content".getBytes(), retrievedData);

        // Update file
        MultipartFile updatedFile = new MockMultipartFile(
                "test.txt",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Updated Content".getBytes()
        );
        fileStorageService.updateFile(fileId, updatedFile);

        // Verify update
        byte[] updatedData = fileStorageService.getFile(fileId);
        assertArrayEquals("Updated Content".getBytes(), updatedData);

        // Delete file
        boolean deleted = fileStorageService.deleteFile(fileId);
        assertTrue(deleted);

        // Verify deletion
        ApiException exception = assertThrows(ApiException.class,
                () -> fileStorageService.getFile(fileId));
        assertEquals("File not found", exception.getErrorResponse().getDescription());
    }
}