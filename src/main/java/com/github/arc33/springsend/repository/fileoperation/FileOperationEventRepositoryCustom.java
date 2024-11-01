package com.github.arc33.springsend.repository.fileoperation;

import java.time.Instant;
import java.util.Map;

public interface FileOperationEventRepositoryCustom {
    Map<String, Long> getOperationStats(Instant since);
}