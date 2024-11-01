package com.github.arc33.springsend;

import com.github.arc33.springsend.config.SecurityConfig;
import com.github.arc33.springsend.filter.JwtTokenFilter;
import com.github.arc33.springsend.repository.file.storage.EtcdFileStorage;
import com.github.arc33.springsend.repository.file.storage.FileStorageRepository;
import com.github.arc33.springsend.service.blacklist.TokenBlacklistService;
import io.etcd.jetcd.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CockroachContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.Mockito.mock;

@TestConfiguration
public abstract class BaseIntegrationTest {
    @Container
    static CockroachContainer db = new CockroachContainer(DockerImageName.parse("cockroachdb/cockroach:latest"))
            .withCommand("start-single-node --insecure")
            .withDatabaseName("defaultdb")
            .withExposedPorts(26257);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> db.getJdbcUrl() + "?sslmode=disable");
        registry.add("spring.datasource.username", () -> db.getUsername());
        registry.add("spring.datasource.password", () -> db.getPassword());
        registry.add("spring.datasource.driver-class-name", () -> db.getDriverClassName());
        registry.add("aws.cognito.clientId", () -> "test-client-id");
        registry.add("aws.cognito.userPoolId", () -> "test-pool-id");
        registry.add("aws.region", () -> "us-east-1");
        registry.add("aws.accessKeyId", () -> "test");
        registry.add("aws.secretKey", () -> "test");
    }

    @TestConfiguration
    public static class CommonTestConfig {
        @Bean(destroyMethod = "close")
        public Client etcdClient(
                @Value("${etcd.endpoints}") String endpoints) {
            return Client.builder()
                    .endpoints(endpoints)
                    .build();
        }

        @Bean
        public FileStorageRepository fileStorageRepository(Client etcdClient) {
            return new EtcdFileStorage(etcdClient);
        }

        @Bean
        public TokenBlacklistService tokenBlacklistService() {
            return mock(TokenBlacklistService.class);
        }

        @Bean
        public JwtTokenFilter jwtTokenFilter() {
            return mock(JwtTokenFilter.class);
        }

        @Bean
        public SecurityConfig securityConfig() {
            return mock(SecurityConfig.class);
        }
    }
}
