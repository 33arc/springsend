package com.github.arc33.springsend.repository.file;

import com.github.arc33.springsend.model.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long>,FileMetadataRepositoryCustom {
    FileMetadata findByFilename(String fileName);

    @Modifying
    @Query("UPDATE FileMetadata c SET " +
            "c.filename = :name, " +
            "c.description = :description, " +
            "c.assignedUser = :assignedUser, " +
            "c.uploader = :uploader, " +
            "c.expiryDate = :expiryDate " +
            "WHERE c.id = :id")
    FileMetadata updateFileMetadataById(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("description") String description,
            @Param("assignedUser") String assignedUser,
            @Param("uploader") String uploader,
            @Param("expiryDate") LocalDateTime expiryDate
    );

    boolean existsByIdAndUploader(Long fileId, String username);

    Page<FileMetadata> findAllByAssignedUser(String username, Pageable pageable);

    String findFilenameById(Long id);

}
