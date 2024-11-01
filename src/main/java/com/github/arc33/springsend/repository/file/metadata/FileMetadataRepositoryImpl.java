package com.github.arc33.springsend.repository.file;

import com.github.arc33.springsend.dto.file.FileMetadataDto;
import com.github.arc33.springsend.mapper.FileMetadataMapper;
import com.github.arc33.springsend.model.Category;
import com.github.arc33.springsend.model.FileMetadata;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class FileMetadataRepositoryImpl implements FileMetadataRepositoryCustom {

    @PersistenceContext(unitName = "filePersistenceUnit")
    private EntityManager entityManager;

    @Autowired
    private FileMetadataMapper metadataMapper;

    @Override
    public Page<FileMetadataDto> searchFiles(String uploader, String assignedUser, String category, String searchTerm, int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FileMetadata> query = cb.createQuery(FileMetadata.class);
        Root<FileMetadata> root = query.from(FileMetadata.class);

        List<Predicate> predicates = new ArrayList<>();

        if (uploader != null && !uploader.isEmpty()) {
            predicates.add(cb.equal(root.get("uploader"), uploader));
        }

        if (assignedUser != null && !assignedUser.isEmpty()) {
            predicates.add(cb.equal(root.get("assignedUser"), assignedUser));
        }

        if (category != null && !category.isEmpty()) {
            Join<FileMetadata, Category> categoryJoin = root.join("categories");
            predicates.add(cb.equal(categoryJoin.get("name"), category));
        }

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("filename")), likePattern),
                    cb.like(cb.lower(root.get("description")), likePattern)
            ));
        }

        query.where(predicates.toArray(new Predicate[0]));

        List<FileMetadata> result = entityManager.createQuery(query)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        // Count query for total elements
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<FileMetadata> countRoot = countQuery.from(FileMetadata.class);
        countQuery.select(cb.count(countRoot)).where(predicates.toArray(new Predicate[0]));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        // Convert FileMetadata to FileMetadataDto
        List<FileMetadataDto> dtoResults = result.stream()
                .map(this::convertToDto)
                .toList();

        return new PageImpl<>(dtoResults, PageRequest.of(page, size), totalElements);
    }

    private FileMetadataDto convertToDto(FileMetadata fileMetadata) {
        return metadataMapper.toDto(fileMetadata);
    }
}