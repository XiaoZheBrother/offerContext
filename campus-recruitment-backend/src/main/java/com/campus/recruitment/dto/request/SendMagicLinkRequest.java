package com.campus.recruitment.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMagicLinkRequest {
    @NotBlank @Email
    private String email;
}
