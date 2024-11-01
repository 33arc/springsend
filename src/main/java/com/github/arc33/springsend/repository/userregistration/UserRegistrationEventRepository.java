package com.github.arc33.springsend.repository.userregistrations;

import com.github.arc33.springsend.domain.event.UserRegistrationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel="userRegistrationEvents",path="userRegistrationEvents")
public interface UserRegistrationEventRepository extends JpaRepository<UserRegistrationEvent, Long> {
    List<UserRegistrationEvent> findByUsername(String username);
}