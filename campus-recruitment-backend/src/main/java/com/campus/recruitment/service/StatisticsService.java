package com.campus.recruitment.service;

import com.campus.recruitment.dto.response.StatisticsResponse;
import com.campus.recruitment.dto.response.StatisticsResponse.TopCompanyItem;
import com.campus.recruitment.repository.AnnouncementRepository;
import com.campus.recruitment.repository.ClickLogRepository;
import com.campus.recruitment.repository.PageViewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsService {

    private final AnnouncementRepository announcementRepository;
    private final PageViewRepository pageViewRepository;
    private final ClickLogRepository clickLogRepository;

    public StatisticsService(AnnouncementRepository announcementRepository,
                             PageViewRepository pageViewRepository,
                             ClickLogRepository clickLogRepository) {
        this.announcementRepository = announcementRepository;
        this.pageViewRepository = pageViewRepository;
        this.clickLogRepository = clickLogRepository;
    }

    public StatisticsResponse getDashboardStatistics() {
        StatisticsResponse response = new StatisticsResponse();
        response.setOnlineCount(announcementRepository.countByOnlineStatus((short) 1));
        response.setTodayPv(pageViewRepository.countTodayPv());
        response.setTodayUv(pageViewRepository.countTodayUv());
        response.setTodayClickCount(clickLogRepository.countTodayClicks());
        response.setTopCompanies(getTopCompanies("7days", 10));
        return response;
    }

    public List<TopCompanyItem> getTopCompanies(String dimension, int limit) {
        List<Object[]> results;

        switch (dimension) {
            case "today":
                LocalDateTime startOfToday = LocalDateTime.now().with(LocalTime.MIN);
                results = clickLogRepository.findTopCompaniesByClickTimeAfter(startOfToday, limit);
                break;
            case "7days":
                LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
                results = clickLogRepository.findTopCompaniesByClickTimeAfter(sevenDaysAgo, limit);
                break;
            case "all":
                results = clickLogRepository.findTopCompaniesAllTime(limit);
                break;
            default:
                LocalDateTime defaultStart = LocalDateTime.now().minusDays(7);
                results = clickLogRepository.findTopCompaniesByClickTimeAfter(defaultStart, limit);
                break;
        }

        List<TopCompanyItem> items = new ArrayList<>();
        for (Object[] row : results) {
            TopCompanyItem item = new TopCompanyItem();
            item.setCompanyName((String) row[1]);
            item.setClickCount(((Number) row[2]).longValue());
            items.add(item);
        }
        return items;
    }
}
