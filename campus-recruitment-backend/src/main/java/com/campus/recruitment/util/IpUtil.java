package com.campus.recruitment.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    private static final String[] HEADER_NAMES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    };

    public static String getClientIp(HttpServletRequest request) {
        for (String header : HEADER_NAMES) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
