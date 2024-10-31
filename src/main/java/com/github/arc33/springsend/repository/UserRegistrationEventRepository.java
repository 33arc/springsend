package com.github.arc33.springsend.repository;

import com.github.arc33.springsend.domain.event.UserRegistrationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRegistrationEventRepository extends JpaRepository<UserRegistrationEvent, Long> {
    List<UserRegistrationEvent> findByUsername(String username);
}