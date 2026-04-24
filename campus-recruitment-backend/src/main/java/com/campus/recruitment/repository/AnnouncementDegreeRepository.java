package com.campus.recruitment.repository;

import com.campus.recruitment.entity.AnnouncementDegree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementDegreeRepository extends JpaRepository<AnnouncementDegree, Integer> {
    List<AnnouncementDegree> findByAnnouncementId(Integer announcementId);
    void deleteByAnnouncementId(Integer announcementId);
}
