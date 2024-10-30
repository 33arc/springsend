package com.github.arc33.springsend.service.blacklist;

import com.github.arc33.springsend.repository.blacklist.BlacklistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EtcdTokenBlacklistServiceTest {
    @Mock
    private BlacklistRepository blacklistRepository;

    @InjectMocks
    private EtcdTokenBlacklistService tokenBlacklistService;

    @Test
    void addToBlacklist_ShouldCallRepository() {
        String token = "test-token";
        tokenBlacklistService.addToBlacklist(token);
        verify(blacklistRepository).addToBlacklist(token, 24 * 60 * 60);
    }

    @Test
    void isBlacklisted_ShouldReturnRepositoryResponse() {
        String token = "test-token";
        when(blacklistRepository.isBlacklisted(token)).thenReturn(true);
        assertTrue(tokenBlacklistService.isBlacklisted(token));
        verify(blacklistRepository).isBlacklisted(token);
    }

    @Test
    void cleanupBlacklist_ShouldCallRepository() {
        tokenBlacklistService.cleanupBlacklist();
        verify(blacklistRepository).cleanup();
    }
}