package com.campus.recruitment.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AdminLoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}
