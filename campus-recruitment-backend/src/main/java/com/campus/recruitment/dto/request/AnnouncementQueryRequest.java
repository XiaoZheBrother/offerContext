package com.campus.recruitment.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class AnnouncementQueryRequest {
    private String keyword;           // Search by company name
    private List<Integer> classTypeIds;   // Filter by graduation year IDs (multi-select)
    private List<Integer> campusTypeIds;  // Filter by batch IDs (multi-select)
    private List<Integer> cityIds;        // Filter by city IDs (multi-select)
    private String applyStatus;       // Filter: all / ongoing / expired
    private Integer page = 1;         // Page number (1-based)
    private Integer pageSize = 20;    // Items per page
}
