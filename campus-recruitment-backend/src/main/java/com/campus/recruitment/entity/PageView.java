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
@Table(name = "page_views")
public class PageView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visitor_id", length = 255)
    private String visitorId;

    @Column(name = "page_url", columnDefinition = "TEXT")
    private String pageUrl;

    @Column(name = "page_type", length = 100)
    private String pageType;

    @Column(columnDefinition = "TEXT")
    private String referer;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "visit_time")
    private LocalDateTime visitTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
