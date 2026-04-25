package com.campus.recruitment.dto.response;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String nickname;
    private String avatarUrl;
}
