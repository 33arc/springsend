package com.github.arc33.springsend.repository.file;

import com.github.arc33.springsend.model.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@RepositoryRestResource(collectionResourceRel="fileMetadata",path="fileMetadata")
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long>,FileMetadataRepositoryCustom {
    boolean existsByIdAndUploader(Long fileId, String username);
    Page<FileMetadata> findAllByAssignedUser(String username, Pageable pageable);
}
