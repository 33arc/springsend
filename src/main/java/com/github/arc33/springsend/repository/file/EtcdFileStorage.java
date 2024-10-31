package com.github.arc33.springsend.repository.file;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
@Profile("test")
public class EtcdFileStorage implements FileStorageRepository {
    private final Client etcdClient;
    private static final String PREFIX = "/file/";

    public EtcdFileStorage(Client etcdClient) {
        this.etcdClient = etcdClient;
    }

    @Override
    public void save(String name, byte[] data) throws IOException {
        ByteSequence key = ByteSequence.from((PREFIX + name).getBytes());
        ByteSequence value = ByteSequence.from(data);
        try {
            etcdClient.getKVClient().put(key, value).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to save file", e);
        }
    }

    @Override
    public byte[] get(String key) throws IOException {
        ByteSequence keySequence = ByteSequence.from((PREFIX + key).getBytes());
        try {
            GetResponse getResponse = etcdClient.getKVClient().get(keySequence).get();
            List<KeyValue> kvs = getResponse.getKvs();
            if (kvs.isEmpty()) {
                return null;
            }
            return kvs.get(0).getValue().getBytes();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to get file", e);
        }
    }

    @Override
    public void delete(String key) throws IOException {
        ByteSequence keySequence = ByteSequence.from((PREFIX + key).getBytes());
        try {
            etcdClient.getKVClient().delete(keySequence).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to delete file", e);
        }
    }

    @Override
    public boolean exists(String key) throws IOException {
        ByteSequence keySequence = ByteSequence.from((PREFIX + key).getBytes());
        try {
            GetResponse getResponse = etcdClient.getKVClient().get(keySequence).get();
            return !getResponse.getKvs().isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Failed to check file existence", e);
        }
    }

    @Override
    public void close() throws IOException {
        etcdClient.close();
    }
}