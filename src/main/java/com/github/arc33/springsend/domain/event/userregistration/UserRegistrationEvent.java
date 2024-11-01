package com.github.arc33.springsend.domain.event.userregistration;

import com.github.arc33.springsend.domain.event.userregistration.enums.RegistrationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "user_registration_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String username;

    @Column
    private String email;

    @Column
    private String role;

    @Column(name = "user_sub")
    private String userSub;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    @NotNull
    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    // Optional: Add methods for state transitions
    public void markAsCreated(String userSub) {
        this.userSub = userSub;
        this.status = RegistrationStatus.USER_CREATED;
        this.timestamp = Instant.now();
    }

    public void markAsCompleted() {
        this.status = RegistrationStatus.COMPLETED;
        this.timestamp = Instant.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = RegistrationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.timestamp = Instant.now();
    }
}