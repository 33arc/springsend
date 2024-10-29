package com.github.arc33.springsend.repository.blacklist;


import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.options.PutOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Repository
@Profile("test")
@RequiredArgsConstructor
@Slf4j
public class EtcdBlacklistRepository implements BlacklistRepository {
    private final Client etcdClient;
    private static final String PREFIX = "/blacklisted_tokens/";
    private static final Duration GET_TIMEOUT = Duration.ofSeconds(5);

    @Override
    public void addToBlacklist(String token, long ttlSeconds) {
        ByteSequence key = ByteSequence.from((PREFIX+token).getBytes());
        ByteSequence value = ByteSequence.from("blacklisted".getBytes());
        try {
            // Wait for lease grant
            LeaseGrantResponse leaseResponse = etcdClient.getLeaseClient()
                    .grant(ttlSeconds)
                    .get(GET_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            long leaseId = leaseResponse.getID();

            // Wait for put operation to complete
            etcdClient.getKVClient()
                    .put(key, value, PutOption.newBuilder().withLeaseId(leaseId).build())
                    .get(GET_TIMEOUT.getSeconds(), TimeUnit.SECONDS);  // Added .get() to wait for completion

            log.debug("Successfully added token {} to blacklist with lease {}", token, leaseId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while adding token to blacklist", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout while adding token to blacklist", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error adding token to blacklist", e.getCause());
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        ByteSequence key = ByteSequence.from((PREFIX+token).getBytes());
        try {
            GetResponse response = etcdClient.getKVClient()
                    .get(key)
                    .get(GET_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

            return !response.getKvs().isEmpty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while checking token blacklist status", e);
            // In case of error, we might want to fail closed (assume token is blacklisted)
            return true;
        } catch (TimeoutException e) {
            log.error("Timeout while checking token blacklist status", e);
            return true;
        } catch (ExecutionException e) {
            log.error("Error checking token blacklist status", e.getCause());
            return true;
        }
    }

    @Override
    public void cleanup() {
        // Etcd automatically removes expired keys (those with expired leases)
    }
}