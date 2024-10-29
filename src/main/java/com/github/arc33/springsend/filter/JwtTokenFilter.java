package com.github.arc33.springsend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.github.arc33.springsend.service.blacklist.TokenBlacklistService;

import java.io.IOException;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractTokenFromRequest(request);

        if(token==null){
            filterChain.doFilter(request, response);
        } else if(token!=null && !tokenBlacklistService.isBlacklisted(token)){
            // token is valid and not blacklisted
            // proceed with request processing
            filterChain.doFilter(request, response);
        } else {
            // token is blacklisted or expired, deny access
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        // get the authorization header
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            // extract the jwt token
            return authHeader.substring(7);
        }

        // auth header is not valid
        return null;
    }
}