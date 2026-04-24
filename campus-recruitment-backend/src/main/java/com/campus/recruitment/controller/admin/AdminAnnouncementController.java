package com.campus.recruitment.controller.admin;

import com.campus.recruitment.common.ApiResponse;
import com.campus.recruitment.common.PageResponse;
import com.campus.recruitment.dto.request.AnnouncementCreateRequest;
import com.campus.recruitment.dto.response.AnnouncementListResponse;
import com.campus.recruitment.service.AdminAnnouncementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/announcements")
public class AdminAnnouncementController {

    @Autowired
    private AdminAnnouncementService adminAnnouncementService;

    @GetMapping
    public ApiResponse<PageResponse<AnnouncementListResponse>> getAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        PageResponse<AnnouncementListResponse> result =
                adminAnnouncementService.getAnnouncementsForAdmin(page, size, keyword, status);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<Integer> createAnnouncement(@Valid @RequestBody AnnouncementCreateRequest request) {
        int count = adminAnnouncementService.createAnnouncement(request);
        return ApiResponse.success(count);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateAnnouncement(@PathVariable Integer id,
                                                @Valid @RequestBody AnnouncementCreateRequest request) {
        adminAnnouncementService.updateAnnouncement(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAnnouncement(@PathVariable Integer id) {
        adminAnnouncementService.deleteAnnouncement(id);
        return ApiResponse.success();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> toggleOnlineStatus(@PathVariable Integer id,
                                                @RequestParam Short onlineStatus) {
        adminAnnouncementService.toggleOnlineStatus(id, onlineStatus);
        return ApiResponse.success();
    }
}
