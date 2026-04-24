package com.campus.recruitment.repository;

import com.campus.recruitment.entity.AnnouncementIndustryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementIndustryTypeRepository extends JpaRepository<AnnouncementIndustryType, Integer> {
    List<AnnouncementIndustryType> findByAnnouncementId(Integer announcementId);
    void deleteByAnnouncementId(Integer announcementId);
}
