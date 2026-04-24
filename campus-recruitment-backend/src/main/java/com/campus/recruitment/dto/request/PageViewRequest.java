package com.campus.recruitment.dto.request;

import lombok.Data;

@Data
public class PageViewRequest {
    private String pageUrl;
    private String pageType;  // "list" or "detail"
    private String referer;
}
