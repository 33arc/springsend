package com.github.arc33.springsend.repository.blacklist;

import io.etcd.jetcd.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@Profile("test")
public class EtcdBlacklistRepositoryTest {

    @Container
    public GenericContainer<?> etcdContainer = new GenericContainer<>("quay.io/coreos/etcd:v3.5.0")
            .withExposedPorts(2379)
            .withCommand(
                    "/usr/local/bin/etcd",
                    "--listen-client-urls=http://0.0.0.0:2379",
                    "--advertise-client-urls=http://0.0.0.0:2379"
            );

    private EtcdBlacklistRepository blacklistRepository;

    @BeforeEach
    void setUp() {
        String etcdUrl = String.format("http://%s:%d",
                etcdContainer.getHost(),
                etcdContainer.getMappedPort(2379));
        Client etcdClient = Client.builder().endpoints(etcdUrl).build();
        blacklistRepository = new EtcdBlacklistRepository(etcdClient);
    }

    @Test
    void testAddAndIsBlacklisted() throws Exception {
        // Given
        String token = "sometoken";
        Long seconds = 300L; // 5 minutes

        // When
        blacklistRepository.addToBlacklist(token, seconds);

        // Then
        assertTrue(blacklistRepository.isBlacklisted(token));

        // Optional: Verify it stays blacklisted for a short while
        Thread.sleep(1000); // Wait 1 second
        assertTrue(blacklistRepository.isBlacklisted(token));
    }

    @Test
    void testExpiration() throws Exception {
        // Given
        String token = "expiringtoken";
        Long seconds = 1L; // 1 second TTL

        // When
        blacklistRepository.addToBlacklist(token, seconds);
        assertTrue(blacklistRepository.isBlacklisted(token));

        // Then
        Thread.sleep(3000); // Wait 3 seconds
        assertFalse(blacklistRepository.isBlacklisted(token));
    }
}