package com.campus.recruitment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "click_logs")
public class ClickLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "announcement_id")
    private Integer announcementId;

    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "visitor_id", length = 255)
    private String visitorId;

    @Column(name = "click_type", length = 50)
    private String clickType;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "click_time")
    private LocalDateTime clickTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
