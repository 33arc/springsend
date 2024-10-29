package com.github.arc33.springsend.repository.blacklist;


import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.options.PutOption;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
@RequiredArgsConstructor
public class EtcdBlacklistRepository implements BlacklistRepository {
    private final Client etcdClient;
    private static final String PREFIX = "/blacklisted_tokens/";

    @Override
    public void addToBlacklist(String token, long ttlSeconds) {
        ByteSequence key = ByteSequence.from((PREFIX+token).getBytes());
        ByteSequence value = ByteSequence.from("blacklisted".getBytes());
        try {
            LeaseGrantResponse response = etcdClient.getLeaseClient().grant(ttlSeconds).get();
            long leaseId = response.getID();
            etcdClient.getKVClient().put(key,value, PutOption.newBuilder().withLeaseId(leaseId).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        ByteSequence key = ByteSequence.from((PREFIX+token).getBytes());
        try {
            return !etcdClient.getKVClient().get(key).get().getKvs().isEmpty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cleanup() {
        // Etcd automatically removes expired keys (those with expired leases)
    }
}