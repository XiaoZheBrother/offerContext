package com.campus.recruitment.dto.response;

import com.campus.recruitment.entity.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private Integer announcementId;
    private String companyName;
    private String announcementName;
    private ApplicationStatus status;
    private String notes;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
