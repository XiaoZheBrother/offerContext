package com.campus.recruitment.controller;

import com.campus.recruitment.common.ApiResponse;
import com.campus.recruitment.dto.request.ApplicationCreateRequest;
import com.campus.recruitment.dto.request.ApplicationToggleRequest;
import com.campus.recruitment.dto.request.ApplicationUpdateRequest;
import com.campus.recruitment.dto.response.ApplicationResponse;
import com.campus.recruitment.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/toggle")
    public ApiResponse<String> toggleApplication(@Valid @RequestBody ApplicationToggleRequest request) {
        Long userId = getCurrentUserId();
        String result = applicationService.toggleApplication(userId, request);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<ApplicationResponse> createApplication(@Valid @RequestBody ApplicationCreateRequest request) {
        Long userId = getCurrentUserId();
        ApplicationResponse response = applicationService.createApplication(userId, request);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<ApplicationResponse>> getApplications() {
        Long userId = getCurrentUserId();
        List<ApplicationResponse> applications = applicationService.getApplications(userId);
        return ApiResponse.success(applications);
    }

    @PutMapping("/{id}")
    public ApiResponse<ApplicationResponse> updateApplication(
            @PathVariable Long id,
            @RequestBody ApplicationUpdateRequest request) {
        Long userId = getCurrentUserId();
        ApplicationResponse response = applicationService.updateApplication(userId, id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteApplication(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        applicationService.deleteApplication(userId, id);
        return ApiResponse.success("删除成功");
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
