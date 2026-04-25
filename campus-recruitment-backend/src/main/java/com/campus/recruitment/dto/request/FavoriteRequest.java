package com.campus.recruitment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteRequest {
    @NotNull
    private Integer announcementId;
}
