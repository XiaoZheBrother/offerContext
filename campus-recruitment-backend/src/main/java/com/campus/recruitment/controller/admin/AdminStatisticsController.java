package com.campus.recruitment.controller.admin;

import com.campus.recruitment.common.ApiResponse;
import com.campus.recruitment.dto.response.StatisticsResponse;
import com.campus.recruitment.dto.response.StatisticsResponse.TopCompanyItem;
import com.campus.recruitment.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping
    public ApiResponse<StatisticsResponse> getDashboardStatistics() {
        StatisticsResponse stats = statisticsService.getDashboardStatistics();
        return ApiResponse.success(stats);
    }

    @GetMapping("/top-companies")
    public ApiResponse<List<TopCompanyItem>> getTopCompanies(
            @RequestParam(defaultValue = "7days") String dimension,
            @RequestParam(defaultValue = "10") int limit) {
        List<TopCompanyItem> topCompanies = statisticsService.getTopCompanies(dimension, limit);
        return ApiResponse.success(topCompanies);
    }
}
