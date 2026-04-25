package com.campus.recruitment.controller;

import com.campus.recruitment.common.ApiResponse;
import com.campus.recruitment.dto.request.SendMagicLinkRequest;
import com.campus.recruitment.dto.response.AuthResponse;
import com.campus.recruitment.dto.response.SendMagicLinkResponse;
import com.campus.recruitment.dto.response.UserResponse;
import com.campus.recruitment.service.UserAuthService;
import com.campus.recruitment.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserAuthService userAuthService;

    @PostMapping("/send-magic-link")
    public ApiResponse<SendMagicLinkResponse> sendMagicLink(
            @Valid @RequestBody SendMagicLinkRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = IpUtil.getClientIp(httpRequest);
        SendMagicLinkResponse response = userAuthService.sendMagicLink(request, clientIp);
        return ApiResponse.success(response);
    }

    @GetMapping("/verify")
    public ApiResponse<AuthResponse> verifyMagicLink(@RequestParam String token) {
        AuthResponse response = userAuthService.verifyMagicLink(token);
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        // 前端删除JWT即可，本期不做服务端黑名单
        return ApiResponse.success("已登出");
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ApiResponse.error(401, "未登录");
        }

        // 从principal中获取userId（JwtAuthenticationFilter设置）
        Long userId = (Long) auth.getPrincipal();
        UserResponse user = userAuthService.getCurrentUser(userId);
        return ApiResponse.success(user);
    }
}
