package com.github.arc33.springsend.service.blacklist;

public interface TokenBlacklistService {
    void addToBlacklist(String token);
    boolean isBlacklisted(String token);
    void cleanupBlacklist();
}