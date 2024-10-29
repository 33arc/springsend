package com.github.arc33.springsend.service.blacklist;

import com.github.arc33.springsend.repository.blacklist.BlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
@RequiredArgsConstructor
public class EtcdTokenBlacklistService implements TokenBlacklistService {
    private final BlacklistRepository blacklistRepository;
    private static final long TTL_SECONDS = 24 * 60 * 60; // 24 hours

    @Override
    public void addToBlacklist(String token) {
        blacklistRepository.addToBlacklist(token,TTL_SECONDS);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklistRepository.isBlacklisted(token);
    }

    @Override
    public void cleanupBlacklist() {
        blacklistRepository.cleanup();
    }
}