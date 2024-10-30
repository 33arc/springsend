package com.github.arc33.springsend.service.blacklist;

import com.github.arc33.springsend.repository.blacklist.BlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
@RequiredArgsConstructor
public class EtcdTokenBlacklistService implements TokenBlacklistService {
    private final BlacklistRepository blacklistRepository;
    @Value("${token.blacklist.ttl-seconds:86400}") // Default 24 hours
    private long TTL_SECONDS;

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