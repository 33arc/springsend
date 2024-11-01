package com.github.arc33.springsend.domain.event.fileoperation;

import com.github.arc33.springsend.domain.event.fileoperation.enums.FileOperationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "file_operation_events")
@Data
@Builder
@NoArgsConstructor  // Required for JPA
@AllArgsConstructor // Required for Builder
public class FileOperationEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;

    @Enumerated(EnumType.STRING)
    private FileOperationType operationType;

    private Instant timestamp;
}