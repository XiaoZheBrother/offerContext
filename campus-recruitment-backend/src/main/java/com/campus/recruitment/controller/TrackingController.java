package com.campus.recruitment.controller;

import com.campus.recruitment.common.ApiResponse;
import com.campus.recruitment.dto.request.ClickLogRequest;
import com.campus.recruitment.dto.request.PageViewRequest;
import com.campus.recruitment.service.ClickLogService;
import com.campus.recruitment.service.PageViewService;
import com.campus.recruitment.util.IpUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrackingController {

    @Autowired
    private ClickLogService clickLogService;

    @Autowired
    private PageViewService pageViewService;

    @PostMapping("/click-logs")
    public ApiResponse<Void> recordClick(@RequestBody ClickLogRequest request, HttpServletRequest httpRequest) {
        String visitorId = getVisitorId(httpRequest);
        String ipAddress = IpUtil.getClientIp(httpRequest);
        clickLogService.recordClick(request, visitorId, ipAddress);
        return ApiResponse.success();
    }

    @PostMapping("/page-views")
    public ApiResponse<Void> recordPageView(@RequestBody PageViewRequest request, HttpServletRequest httpRequest) {
        String visitorId = getVisitorId(httpRequest);
        String ipAddress = IpUtil.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        pageViewService.recordPageView(request, visitorId, ipAddress, userAgent);
        return ApiResponse.success();
    }

    private String getVisitorId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("visitor_id".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
