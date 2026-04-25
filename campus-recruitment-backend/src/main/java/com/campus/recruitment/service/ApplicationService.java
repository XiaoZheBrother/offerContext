package com.campus.recruitment.service;

import com.campus.recruitment.dto.request.ApplicationCreateRequest;
import com.campus.recruitment.dto.request.ApplicationToggleRequest;
import com.campus.recruitment.dto.request.ApplicationUpdateRequest;
import com.campus.recruitment.dto.response.ApplicationResponse;
import com.campus.recruitment.entity.ApplicationRecord;
import com.campus.recruitment.entity.ApplicationStatus;
import com.campus.recruitment.entity.Announcement;
import com.campus.recruitment.exception.BusinessException;
import com.campus.recruitment.repository.AnnouncementRepository;
import com.campus.recruitment.repository.ApplicationRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRecordRepository applicationRecordRepository;
    private final AnnouncementRepository announcementRepository;

    public ApplicationService(ApplicationRecordRepository applicationRecordRepository,
                              AnnouncementRepository announcementRepository) {
        this.applicationRecordRepository = applicationRecordRepository;
        this.announcementRepository = announcementRepository;
    }

    /**
     * Toggle投递状态（卡片开关用）
     * - 无记录 → 创建(applied)
     * - 有记录且status=applied → 删除
     * - 有记录且status!=applied → 返回400
     */
    @Transactional
    public String toggleApplication(Long userId, ApplicationToggleRequest request) {
        Integer announcementId = request.getAnnouncementId();
        Optional<ApplicationRecord> existing = applicationRecordRepository
                .findByUserIdAndAnnouncementId(userId, announcementId);

        if (existing.isEmpty()) {
            // 创建记录
            ApplicationRecord record = new ApplicationRecord();
            record.setUserId(userId);
            record.setAnnouncementId(announcementId);
            record.setStatus(ApplicationStatus.APPLIED);
            record.setAppliedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            applicationRecordRepository.save(record);
            return "applied";
        }

        ApplicationRecord record = existing.get();
        if (record.getStatus() == ApplicationStatus.APPLIED) {
            // 删除记录
            applicationRecordRepository.deleteByUserIdAndAnnouncementId(userId, announcementId);
            applicationRecordRepository.flush();
            return "removed";
        }

        // 非applied状态，不允许toggle
        throw new BusinessException(400, "当前投递状态为「" + record.getStatus().name()
                + "」，请在投递记录页面管理");
    }

    /**
     * 创建投递记录
     */
    @Transactional
    public ApplicationResponse createApplication(Long userId, ApplicationCreateRequest request) {
        Integer announcementId = request.getAnnouncementId();

        Optional<ApplicationRecord> existing = applicationRecordRepository
                .findByUserIdAndAnnouncementId(userId, announcementId);

        ApplicationRecord record;
        if (existing.isPresent()) {
            // 更新已有记录
            record = existing.get();
            if (request.getStatus() != null) {
                record.setStatus(request.getStatus());
            }
            if (request.getNotes() != null) {
                record.setNotes(request.getNotes());
            }
            record.setUpdatedAt(LocalDateTime.now());
        } else {
            // 创建新记录
            record = new ApplicationRecord();
            record.setUserId(userId);
            record.setAnnouncementId(announcementId);
            record.setStatus(request.getStatus() != null ? request.getStatus() : ApplicationStatus.APPLIED);
            record.setNotes(request.getNotes());
            record.setAppliedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
        }

        applicationRecordRepository.save(record);
        return toResponse(record);
    }

    /**
     * 获取投递记录列表
     */
    public List<ApplicationResponse> getApplications(Long userId) {
        return applicationRecordRepository.findByUserIdOrderByAppliedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 更新投递状态/备注
     */
    @Transactional
    public ApplicationResponse updateApplication(Long userId, Long recordId, ApplicationUpdateRequest request) {
        ApplicationRecord record = applicationRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("投递记录不存在"));

        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此记录");
        }

        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            record.setNotes(request.getNotes());
        }
        record.setUpdatedAt(LocalDateTime.now());

        applicationRecordRepository.save(record);
        return toResponse(record);
    }

    /**
     * 删除投递记录
     */
    @Transactional
    public void deleteApplication(Long userId, Long recordId) {
        ApplicationRecord record = applicationRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("投递记录不存在"));

        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此记录");
        }

        applicationRecordRepository.delete(record);
        applicationRecordRepository.flush();
    }

    /**
     * 检查是否已投递
     */
    public Optional<ApplicationRecord> findByUserAndAnnouncement(Long userId, Integer announcementId) {
        if (userId == null) return Optional.empty();
        return applicationRecordRepository.findByUserIdAndAnnouncementId(userId, announcementId);
    }

    private ApplicationResponse toResponse(ApplicationRecord record) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(record.getId());
        response.setAnnouncementId(record.getAnnouncementId());
        response.setStatus(record.getStatus());
        response.setNotes(record.getNotes());
        response.setAppliedAt(record.getAppliedAt());
        response.setUpdatedAt(record.getUpdatedAt());

        // 填充公告信息
        announcementRepository.findById(record.getAnnouncementId()).ifPresent(announcement -> {
            response.setCompanyName(announcement.getCompany() != null
                    ? announcement.getCompany().getName() : "");
            response.setAnnouncementName(announcement.getName());
        });

        return response;
    }
}
