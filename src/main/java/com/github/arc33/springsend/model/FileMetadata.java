package com.github.arc33.springsend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name="files",indexes = {
        @Index(name="idx_uploader",columnList = "uploader"),
        @Index(name="idx_assignedUser",columnList = "assignedUser")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true, fluent = false)
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String assignedUser;

    @Column(columnDefinition = "TEXT",nullable = false)
    private String fileKey;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String filename;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String uploader;

    @Column(nullable = false)
    private boolean expires;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @ManyToMany
    @JoinTable(
            name = "categories_relations",
            joinColumns = @JoinColumn(name = "file_id"),
            inverseJoinColumns = @JoinColumn(name = "cat_id")
    )
    private Set<Category> categories;
}