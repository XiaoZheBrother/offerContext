package com.campus.recruitment.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class StatisticsResponse {
    private long onlineCount;
    private long todayPv;
    private long todayUv;
    private long todayClickCount;
    private List<TopCompanyItem> topCompanies;

    @Data
    public static class TopCompanyItem {
        private String companyName;
        private long clickCount;
    }
}
