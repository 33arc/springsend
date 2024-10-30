package com.github.arc33.springsend.model;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface User {
    String getUsername();
    String getEmail();
    String getRole();
    String getStatus();
    Collection<? extends GrantedAuthority> getAuthorities();
}
