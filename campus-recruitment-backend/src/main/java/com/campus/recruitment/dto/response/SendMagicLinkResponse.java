package com.campus.recruitment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendMagicLinkResponse {
    private String message;
    // 开发模式下直接返回token，生产环境删除此字段
    private String token;
}
