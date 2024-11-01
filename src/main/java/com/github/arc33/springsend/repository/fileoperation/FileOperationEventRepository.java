package com.github.arc33.springsend.repository.fileoperation;

import com.github.arc33.springsend.domain.event.fileoperation.FileOperationEvent;
import com.github.arc33.springsend.domain.event.fileoperation.enums.FileOperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.Instant;
import java.util.List;

@RepositoryRestResource(collectionResourceRel="fileOperations",path="fileOperations")
public interface FileOperationEventRepository extends JpaRepository<FileOperationEvent, Long> {

    List<FileOperationEvent> findByFileId(Long fileId);

    List<FileOperationEvent> findByFileIdAndOperationType(Long fileId, FileOperationType operationType);

    @Query("SELECT e FROM FileOperationEvent e WHERE e.fileId = :fileId AND e.timestamp >= :since")
    List<FileOperationEvent> findRecentOperations(@Param("fileId") Long fileId, @Param("since") Instant since);

    @Query("SELECT COUNT(e) FROM FileOperationEvent e WHERE e.operationType = :operationType AND e.timestamp >= :since")
    long countOperationsSince(@Param("operationType") FileOperationType operationType, @Param("since") Instant since);

    // For audit purposes
    List<FileOperationEvent> findByTimestampBetween(Instant start, Instant end);

    // For cleanup
    void deleteByTimestampBefore(Instant timestamp);
}