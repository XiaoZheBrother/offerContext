package com.campus.recruitment.repository;

import com.campus.recruitment.entity.AnnouncementJobCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementJobCategoryRepository extends JpaRepository<AnnouncementJobCategory, Integer> {
    List<AnnouncementJobCategory> findByAnnouncementId(Integer announcementId);
    void deleteByAnnouncementId(Integer announcementId);
}
