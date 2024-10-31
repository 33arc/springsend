package com.github.arc33.springsend.repository.file;

import java.io.IOException;

public interface FileStorageRepository {
    void save(String key, byte[] data) throws IOException, IOException;
    byte[] get(String key) throws IOException;
    void delete(String key) throws IOException;
    boolean exists(String key) throws IOException;
    void close() throws IOException;
}