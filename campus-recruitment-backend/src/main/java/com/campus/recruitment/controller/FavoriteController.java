package com.campus.recruitment.controller;

import com.campus.recruitment.common.ApiResponse;
import com.campus.recruitment.dto.request.FavoriteRequest;
import com.campus.recruitment.dto.response.FavoriteItemResponse;
import com.campus.recruitment.service.FavoriteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping
    public ApiResponse<String> addFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = getCurrentUserId();
        favoriteService.addFavorite(userId, request.getAnnouncementId());
        return ApiResponse.success("收藏成功");
    }

    @DeleteMapping("/{announcementId}")
    public ApiResponse<String> removeFavorite(@PathVariable Integer announcementId) {
        Long userId = getCurrentUserId();
        favoriteService.removeFavorite(userId, announcementId);
        return ApiResponse.success("取消收藏成功");
    }

    @GetMapping
    public ApiResponse<List<FavoriteItemResponse>> getFavorites() {
        Long userId = getCurrentUserId();
        List<FavoriteItemResponse> favorites = favoriteService.getFavorites(userId);
        return ApiResponse.success(favorites);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
