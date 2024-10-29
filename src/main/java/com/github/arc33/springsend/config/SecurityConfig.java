package com.github.arc33.springsend.config;

import com.github.arc33.springsend.filter.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtTokenFilter jwtTokenFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors->cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf->csrf.disable()) // no session state is maintained on the server
                .headers(headers -> {
                    headers
                            // Configure frame options instead of disabling
                            .frameOptions(frame -> frame
                                    .sameOrigin()  // Allows same-origin framing, more secure than disable()
                            )
                            // Add other security headers
                            .contentSecurityPolicy(csp -> csp
                                    .policyDirectives("default-src 'self'; frame-ancestors 'self'; script-src 'self'")
                            )
                            // HTTP Strict Transport Security
                            .httpStrictTransportSecurity(hsts -> hsts
                                    .includeSubDomains(true)
                                    .preload(true)
                                    .maxAgeInSeconds(31536000)
                            )
                            // Cache control
                            .cacheControl(cache -> cache.disable());
                })
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/v1/**","/authTest").permitAll();
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2->oauth2.jwt(jwt->jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .sessionManagement(k->k.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8000", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new CognitoJwtRoleConverter());
        converter.setPrincipalClaimName("username");
        return converter;
    }

    private class CognitoJwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Add scope as role
            // it assumes that each user only has one group
            // aws.cognito.signin.user.somerole -> ROLE_somerole
            String scopeString = jwt.getClaimAsString("scope");
            if (scopeString != null && !scopeString.isEmpty()) {
                // find the last dot position
                int pos = scopeString.length()-1;
                while(scopeString.charAt(pos)!='.'){
                    --pos;
                }

                authorities.add(new SimpleGrantedAuthority("ROLE_" + scopeString.substring(pos+1)));
            }

            logJwtClaims(jwt,authorities);
            return authorities;
        }
    }
    private void logJwtClaims(Jwt jwt, Collection<GrantedAuthority> authorities) {
        LOG.debug("JWT Claims: {}", jwt.getClaims());
        LOG.debug("Scope: {}", jwt.getClaimAsString("scope"));
        LOG.debug("Cognito Groups: {}", jwt.getClaimAsStringList("cognito:groups"));
        LOG.debug("Granted Authorities: {}", authorities);
    }
}