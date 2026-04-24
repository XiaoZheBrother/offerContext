package com.campus.recruitment.service;

import com.campus.recruitment.dto.request.PageViewRequest;
import com.campus.recruitment.entity.PageView;
import com.campus.recruitment.repository.PageViewRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PageViewService {

    private final PageViewRepository pageViewRepository;

    public PageViewService(PageViewRepository pageViewRepository) {
        this.pageViewRepository = pageViewRepository;
    }

    @Async
    public void recordPageView(PageViewRequest request, String visitorId, String ipAddress, String userAgent) {
        PageView pageView = new PageView();
        pageView.setVisitorId(visitorId);
        pageView.setPageUrl(request.getPageUrl());
        pageView.setPageType(request.getPageType());
        pageView.setReferer(request.getReferer());
        pageView.setUserAgent(userAgent);
        pageView.setIpAddress(ipAddress);
        pageView.setVisitTime(LocalDateTime.now());
        pageView.setCreatedAt(LocalDateTime.now());

        pageViewRepository.save(pageView);
    }
}
