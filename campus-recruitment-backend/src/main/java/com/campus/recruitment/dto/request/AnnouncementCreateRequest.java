package com.campus.recruitment.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Data
public class AnnouncementCreateRequest {
    @NotBlank private String companyName;
    @NotBlank private String name;
    private String detail;            // Rich text content
    private String fromUrl;           // 投递链接
    private String link;              // 宣发网址
    @NotEmpty private List<Integer> classTypeIds;
    @NotEmpty private List<Integer> campusTypeIds;  // Multi-batch → auto-split records
    @NotEmpty private List<Integer> cityIds;
    private List<String> customCities;  // Custom city names to add
    @NotNull private LocalDate publishedAt;
    @NotNull private LocalDate expiredAt;
    @NotBlank private String applyLink;  // from_url field value
    private Short onlineStatus = 0;  // Default offline
    private LocalDate createdAt;     // 发布日期, edit keeps original
}
