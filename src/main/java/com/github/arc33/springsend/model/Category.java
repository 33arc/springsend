package com.github.arc33.springsend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories",indexes = {
        @Index(name="idx_name",columnList = "name"),
        @Index(name="idx_createdBy",columnList = "createdBy")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true, fluent = false)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 32, nullable = false)
    private String name;

    @Column
    private String createdBy;

    @ManyToMany(mappedBy = "categories")
    private Set<FileMetadata> files = new HashSet<>();
}
