package com.campus.recruitment.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AnnouncementDetailResponse {
    private Integer announcementId;
    private String companyName;
    private String name;
    private String detail;           // Rich text
    private String fromUrl;          // 投递链接
    private String link;             // 宣发网址
    private List<String> classTypeNames;
    private List<String> campusTypeNames;
    private List<String> cityNames;
    private List<String> degreeNames;
    private List<String> industryTypeNames;
    private List<String> jobCategoryNames;
    private LocalDate publishedAt;
    private LocalDate expiredAt;
    private String applyStatus;
    private LocalDate createdAt;
    private String salary;
    private String companyWelfare;
    private String classTime;
    private Boolean writtenTest;
    private Boolean acceptWorkExperience;
    private List<String> companyDescriptions;
    private List<String> companyIndustryNames;
}
