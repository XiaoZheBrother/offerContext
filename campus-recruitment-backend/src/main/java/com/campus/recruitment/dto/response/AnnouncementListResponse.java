package com.campus.recruitment.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AnnouncementListResponse {
    private Integer announcementId;
    private String companyName;
    private String name;
    private List<String> classTypeNames;
    private List<String> campusTypeNames;
    private List<String> cityNames;
    private LocalDate expiredAt;
    private String applyStatus;  // "not_started" / "ongoing" / "expired"
    private Short onlineStatus;
    private LocalDate createdAt;
}
