package com.campus.recruitment.dto.request;

import com.campus.recruitment.entity.ApplicationStatus;
import lombok.Data;

@Data
public class ApplicationUpdateRequest {
    private ApplicationStatus status;
    private String notes;
}
