package com.github.arc33.springsend.mapper;

import com.github.arc33.springsend.dto.file.FileMetadataDto;
import com.github.arc33.springsend.dto.file.FileUploadMetadata;
import com.github.arc33.springsend.model.Category;
import com.github.arc33.springsend.model.FileMetadata;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {LocalDateTime.class})
public interface FileMetadataMapper {

    @Mapping(target = "assignedUser", source = "assignedUser")
    @Mapping(target = "categoryIds", expression = "java(mapCategoriesToIds(fileMetadata.getCategories()))")
    FileMetadataDto toDto(FileMetadata fileMetadata);

    @Mapping(target = "assignedUser", source = "assignedUser")
    @Mapping(target = "expires", expression = "java(!fileMetadataDto.isExpires())")
    @Mapping(target = "fileKey", ignore = true)
    @Mapping(target = "timestamp", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "categories", ignore = true)
    FileMetadata toEntity(FileMetadataDto fileMetadataDto);

    @Mapping(target = "filename", source = "fileName")
    @Mapping(target = "categories", ignore = true) // we will handle this in @AfterMapping
    @Mapping(target = "timestamp", expression = "java(LocalDateTime.now())")
    @Mapping(target = "fileKey", ignore = true)
    FileMetadata toMetadata(FileUploadMetadata metadata);

    @AfterMapping
    default void mapUploadCategories(@MappingTarget FileMetadata fileMetadata, FileUploadMetadata fileMetadataDto) {
        if (fileMetadataDto.getCategoryIds() != null) {
            fileMetadata.setCategories(fileMetadataDto.getCategoryIds().stream()
                    .map(id -> {
                        Category category = new Category();
                        category.setId(id);
                        return category;
                    })
                    .collect(Collectors.toSet()));
        }
    }

    @AfterMapping
    default void mapCategories(@MappingTarget FileMetadata fileMetadata, FileMetadataDto fileMetadataDto) {
        if (fileMetadataDto.getCategoryIds() != null) {
            fileMetadata.setCategories(fileMetadataDto.getCategoryIds().stream()
                    .map(id -> {
                        Category category = new Category();
                        category.setId(id);
                        return category;
                    })
                    .collect(Collectors.toSet()));
        }
    }

    default Set<Long> mapCategoriesToIds(Set<Category> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
    }
}