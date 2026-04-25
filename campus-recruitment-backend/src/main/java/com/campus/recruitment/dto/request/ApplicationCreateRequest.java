package com.campus.recruitment.dto.request;

import com.campus.recruitment.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationCreateRequest {
    @NotNull
    private Integer announcementId;

    private ApplicationStatus status;

    private String notes;
}
