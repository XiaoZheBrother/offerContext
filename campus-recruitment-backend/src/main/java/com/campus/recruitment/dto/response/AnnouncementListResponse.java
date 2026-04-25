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
    // 2.0 用户态字段（未登录时默认值）
    private Boolean isFavorited = false;
    private Boolean isApplied = false;
    private String applicationStatus;  // null if not applied
}
