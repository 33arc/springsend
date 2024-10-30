package com.github.arc33.springsend.service.blacklist;

import com.github.arc33.springsend.BaseIntegrationTest;
import com.github.arc33.springsend.SpringsendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@SpringBootTest(classes = {
        SpringsendApplication.class,
        BaseIntegrationTest.CommonTestConfig.class
})
@ActiveProfiles("test")
@Testcontainers
@TestPropertySource(properties = {
        "token.blacklist.ttl-seconds=2"
})
class EtcdTokenBlacklistServiceIntegrationTest extends BaseIntegrationTest {
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
    private EtcdTokenBlacklistService tokenBlacklistService;

    @Test
    void shouldManageTokenBlacklist() {
        String token = "integration-test-token";

        tokenBlacklistService.addToBlacklist(token);
        assertTrue(tokenBlacklistService.isBlacklisted(token));

        tokenBlacklistService.cleanupBlacklist();
        assertTrue(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    void shouldNotFindNonExistentToken() {
        assertFalse(tokenBlacklistService.isBlacklisted("non-existent-token"));
    }

    @Test
    void shouldExpireTokenAfterTTL() throws InterruptedException {
        String token = "expiring-token";
        tokenBlacklistService.addToBlacklist(token);
        assertTrue(tokenBlacklistService.isBlacklisted(token));

        // Wait for token to expire (using shorter TTL for testing)
        Thread.sleep(3000); // Assuming TTL is configured shorter for tests

        tokenBlacklistService.cleanupBlacklist();
        assertFalse(tokenBlacklistService.isBlacklisted(token));
    }
}