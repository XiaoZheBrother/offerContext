package com.campus.recruitment.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FavoriteItemResponse {
    private Long favoriteId;
    private Integer announcementId;
    private String companyName;
    private String announcementName;
    private LocalDate expiredAt;
    private String applyStatus;
    private LocalDateTime favoritedAt;
}
