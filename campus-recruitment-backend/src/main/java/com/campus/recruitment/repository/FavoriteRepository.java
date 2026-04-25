package com.campus.recruitment.repository;

import com.campus.recruitment.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndAnnouncementId(Long userId, Integer announcementId);

    void deleteByUserIdAndAnnouncementId(Long userId, Integer announcementId);

    @Query("SELECT f.announcementId FROM Favorite f WHERE f.userId = :userId")
    Set<Integer> findAnnouncementIdsByUserId(Long userId);
}
