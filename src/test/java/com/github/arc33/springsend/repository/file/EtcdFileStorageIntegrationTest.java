package com.github.arc33.springsend.repository.file;

import io.etcd.jetcd.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class EtcdFileStorageIntegrationTest {

    @Container
    private static final GenericContainer<?> etcdContainer = new GenericContainer<>(DockerImageName.parse("bitnami/etcd:latest"))
            .withExposedPorts(2379)
            .withEnv("ALLOW_NONE_AUTHENTICATION", "yes")
            .withEnv("ETCD_ADVERTISE_CLIENT_URLS", "http://0.0.0.0:2379");

    private EtcdFileStorage fileStorage;
    private Client etcdClient;

    @BeforeEach
    void setUp() {
        String endpoint = String.format("http://%s:%d",
                etcdContainer.getHost(),
                etcdContainer.getMappedPort(2379));

        etcdClient = Client.builder()
                .endpoints(endpoint)
                .build();

        fileStorage = new EtcdFileStorage(etcdClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        fileStorage.close();
    }

    @Test
    void shouldSaveAndRetrieveFile() throws IOException {
        // given
        String fileName = "test.txt";
        byte[] content = "Hello, World!".getBytes();

        // when
        fileStorage.save(fileName, content);
        byte[] retrieved = fileStorage.get(fileName);

        // then
        assertNotNull(retrieved);
        assertTrue(Arrays.equals(content, retrieved));
    }

    @Test
    void shouldReturnNullForNonExistentFile() throws IOException {
        // when
        byte[] retrieved = fileStorage.get("non-existent.txt");

        // then
        assertNull(retrieved);
    }

    @Test
    void shouldCheckFileExistence() throws IOException {
        // given
        String fileName = "exists.txt";
        byte[] content = "test content".getBytes();

        // when
        fileStorage.save(fileName, content);

        // then
        assertTrue(fileStorage.exists(fileName));
        assertFalse(fileStorage.exists("non-existent.txt"));
    }

    @Test
    void shouldDeleteFile() throws IOException {
        // given
        String fileName = "to-delete.txt";
        byte[] content = "delete me".getBytes();
        fileStorage.save(fileName, content);

        // when
        fileStorage.delete(fileName);

        // then
        assertFalse(fileStorage.exists(fileName));
        assertNull(fileStorage.get(fileName));
    }

    @Test
    void shouldHandleLargeFiles() throws IOException {
        // given
        String fileName = "large-file.bin";
        byte[] largeContent = new byte[1024 * 1024]; // 1MB file
        Arrays.fill(largeContent, (byte) 'X');

        // when
        fileStorage.save(fileName, largeContent);
        byte[] retrieved = fileStorage.get(fileName);

        // then
        assertNotNull(retrieved);
        assertTrue(Arrays.equals(largeContent, retrieved));
    }

    @Test
    void shouldHandleSpecialCharactersInFileName() throws IOException {
        // given
        String fileName = "special@#$%^&*.txt";
        byte[] content = "special content".getBytes();

        // when
        fileStorage.save(fileName, content);
        byte[] retrieved = fileStorage.get(fileName);

        // then
        assertNotNull(retrieved);
        assertTrue(Arrays.equals(content, retrieved));
    }
}