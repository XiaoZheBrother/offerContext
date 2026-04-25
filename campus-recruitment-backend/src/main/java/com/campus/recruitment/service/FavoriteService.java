package com.campus.recruitment.service;

import com.campus.recruitment.dto.response.FavoriteItemResponse;
import com.campus.recruitment.entity.Announcement;
import com.campus.recruitment.entity.Favorite;
import com.campus.recruitment.exception.BusinessException;
import com.campus.recruitment.repository.AnnouncementRepository;
import com.campus.recruitment.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AnnouncementRepository announcementRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           AnnouncementRepository announcementRepository) {
        this.favoriteRepository = favoriteRepository;
        this.announcementRepository = announcementRepository;
    }

    /**
     * 添加收藏
     */
    @Transactional
    public void addFavorite(Long userId, Integer announcementId) {
        if (favoriteRepository.existsByUserIdAndAnnouncementId(userId, announcementId)) {
            return; // 已收藏，幂等处理
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setAnnouncementId(announcementId);
        favorite.setCreatedAt(LocalDateTime.now());
        favoriteRepository.save(favorite);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Long userId, Integer announcementId) {
        favoriteRepository.deleteByUserIdAndAnnouncementId(userId, announcementId);
        favoriteRepository.flush();
    }

    /**
     * 获取我的收藏列表，按截止时间排序
     * 排序规则：未截止优先→截止时间升序→已截止→无截止时间
     */
    public List<FavoriteItemResponse> getFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);

        LocalDateTime now = LocalDateTime.now();

        return favorites.stream()
                .map(fav -> {
                    FavoriteItemResponse item = new FavoriteItemResponse();
                    item.setFavoriteId(fav.getId());
                    item.setAnnouncementId(fav.getAnnouncementId());
                    item.setFavoritedAt(fav.getCreatedAt());

                    announcementRepository.findById(fav.getAnnouncementId()).ifPresent(announcement -> {
                        item.setCompanyName(announcement.getCompany() != null
                                ? announcement.getCompany().getName() : "");
                        item.setAnnouncementName(announcement.getName());
                        if (announcement.getExpiredAt() != null) {
                            item.setExpiredAt(announcement.getExpiredAt().toLocalDate());
                        }
                        // 计算applyStatus
                        item.setApplyStatus(calculateApplyStatus(announcement, now));
                    });

                    return item;
                })
                .sorted(getFavoriteComparator(now))
                .collect(Collectors.toList());
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorited(Long userId, Integer announcementId) {
        if (userId == null) return false;
        return favoriteRepository.existsByUserIdAndAnnouncementId(userId, announcementId);
    }

    private String calculateApplyStatus(Announcement announcement, LocalDateTime now) {
        if (announcement.getPublishedAt() != null && now.isBefore(announcement.getPublishedAt())) {
            return "not_started";
        }
        if (announcement.getExpiredAt() != null && now.isAfter(announcement.getExpiredAt())) {
            return "expired";
        }
        return "ongoing";
    }

    private Comparator<FavoriteItemResponse> getFavoriteComparator(LocalDateTime now) {
        return (a, b) -> {
            boolean aExpired = "expired".equals(a.getApplyStatus());
            boolean bExpired = "expired".equals(b.getApplyStatus());

            // 未截止优先
            if (aExpired != bExpired) {
                return aExpired ? 1 : -1;
            }

            // 都未截止：按截止时间升序（最早截止的在前）
            if (!aExpired) {
                if (a.getExpiredAt() != null && b.getExpiredAt() != null) {
                    return a.getExpiredAt().compareTo(b.getExpiredAt());
                }
                if (a.getExpiredAt() != null) return -1;
                if (b.getExpiredAt() != null) return 1;
            }

            // 都已截止或都无截止时间：按收藏时间倒序
            return b.getFavoritedAt().compareTo(a.getFavoritedAt());
        };
    }
}
