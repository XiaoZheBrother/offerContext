package com.campus.recruitment.service;

import com.campus.recruitment.dto.request.ClickLogRequest;
import com.campus.recruitment.entity.ClickLog;
import com.campus.recruitment.repository.ClickLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ClickLogService {

    private final ClickLogRepository clickLogRepository;

    public ClickLogService(ClickLogRepository clickLogRepository) {
        this.clickLogRepository = clickLogRepository;
    }

    @Async
    public void recordClick(ClickLogRequest request, String visitorId, String ipAddress) {
        ClickLog clickLog = new ClickLog();
        clickLog.setAnnouncementId(request.getAnnouncementId());
        clickLog.setVisitorId(visitorId);
        clickLog.setClickType(request.getClickType());
        clickLog.setIpAddress(ipAddress);
        clickLog.setClickTime(LocalDateTime.now());
        clickLog.setCreatedAt(LocalDateTime.now());

        // Set companyId from announcement if available
        // The controller should set companyId on the request or we query it here
        // For now, we query the announcement to get companyId
        clickLog.setCompanyId(null); // Will be set by caller if needed

        clickLogRepository.save(clickLog);
    }

    @Async
    public void recordClick(ClickLogRequest request, String visitorId, String ipAddress, Integer companyId) {
        ClickLog clickLog = new ClickLog();
        clickLog.setAnnouncementId(request.getAnnouncementId());
        clickLog.setCompanyId(companyId);
        clickLog.setVisitorId(visitorId);
        clickLog.setClickType(request.getClickType());
        clickLog.setIpAddress(ipAddress);
        clickLog.setClickTime(LocalDateTime.now());
        clickLog.setCreatedAt(LocalDateTime.now());

        clickLogRepository.save(clickLog);
    }
}
