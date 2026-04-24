package com.campus.recruitment.dto.response;

import lombok.Data;

@Data
public class AdminLoginResponse {
    private String token;
    private String username;
}
