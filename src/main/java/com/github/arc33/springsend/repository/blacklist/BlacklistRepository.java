package com.github.arc33.springsend.repository.blacklist;

public interface BlacklistRepository {
    void addToBlacklist(String token, long ttlSeconds);
    boolean isBlacklisted(String token);
    void cleanup();
}