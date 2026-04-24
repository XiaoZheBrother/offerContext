package com.campus.recruitment.controller.admin;

import com.campus.recruitment.common.ApiResponse;
import com.campus.recruitment.dto.request.AdminLoginRequest;
import com.campus.recruitment.dto.response.AdminLoginResponse;
import com.campus.recruitment.service.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminAuthService.login(request);
        return ApiResponse.success(response);
    }
}
