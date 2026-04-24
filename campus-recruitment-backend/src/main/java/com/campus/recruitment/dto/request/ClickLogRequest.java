package com.campus.recruitment.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class ClickLogRequest {
    @NotNull private Integer announcementId;
    private String clickType;  // "link" or "email"
}
