package com.github.arc33.springsend.repository.file.metadata;

import com.github.arc33.springsend.model.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long>,FileMetadataRepositoryCustom {
    boolean existsByIdAndUploader(Long fileId, String username);
    Page<FileMetadata> findAllByAssignedUser(String username, Pageable pageable);
}
