package com.github.arc33.springsend.model;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

@Value
@RequiredArgsConstructor
public class CognitoUser implements User {
    private final String username;
    private final String email;
    private final String role;
    private final String status;
    private final Collection<? extends GrantedAuthority> authorities;

    public CognitoUser(final String username, final String email, final String role, final String status) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.status = status;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

}
