package com.campus.recruitment.controller;

import com.campus.recruitment.common.ApiResponse;
import com.campus.recruitment.common.PageResponse;
import com.campus.recruitment.dto.request.AnnouncementQueryRequest;
import com.campus.recruitment.dto.response.AnnouncementDetailResponse;
import com.campus.recruitment.dto.response.AnnouncementListResponse;
import com.campus.recruitment.dto.response.FilterOptionsResponse;
import com.campus.recruitment.service.AnnouncementService;
import com.campus.recruitment.service.FilterOptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private FilterOptionService filterOptionService;

    @GetMapping
    public ApiResponse<PageResponse<AnnouncementListResponse>> getAnnouncementList(
            @ModelAttribute AnnouncementQueryRequest queryRequest) {
        PageResponse<AnnouncementListResponse> result = announcementService.getAnnouncementList(queryRequest);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<AnnouncementDetailResponse> getAnnouncementDetail(@PathVariable Integer id) {
        AnnouncementDetailResponse detail = announcementService.getAnnouncementDetail(id);
        return ApiResponse.success(detail);
    }

    @GetMapping("/filter-options")
    public ApiResponse<FilterOptionsResponse> getFilterOptions() {
        FilterOptionsResponse options = filterOptionService.getFilterOptions();
        return ApiResponse.success(options);
    }
}
