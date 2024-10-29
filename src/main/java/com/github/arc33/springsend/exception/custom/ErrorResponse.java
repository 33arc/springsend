package com.github.arc33.springsend.exception.custom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse{
    private String type;
    private String code;
    private String description;
    @JsonIgnore
    private HttpStatus status;
}