package com.campus.recruitment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @Column(name = "announcement_id")
    private Integer announcementId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(length = 255)
    private String salary;

    @Column(name = "company_id")
    private Integer companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @Column(name = "link", columnDefinition = "TEXT")
    private String link;

    @Column(name = "from_url", columnDefinition = "TEXT")
    private String fromUrl;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "exp_type")
    private Short expType;

    @Column
    private Short type;

    @Column(name = "original_jobs", columnDefinition = "TEXT")
    private String originalJobs;

    @Column
    private Short status;

    @Column(name = "online_status")
    private Short onlineStatus;

    @Column(name = "diff_score")
    private Integer diffScore;

    @Column(name = "ref_code", length = 100)
    private String refCode;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "class_time")
    private String classTime;

    @Column(name = "written_test")
    private Short writtenTest;

    @Column(name = "company_welfare", columnDefinition = "TEXT")
    private String companyWelfare;

    @Column(name = "accept_work_experience")
    private Short acceptWorkExperience;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
