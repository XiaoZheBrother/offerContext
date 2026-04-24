package com.campus.recruitment.repository;

import com.campus.recruitment.entity.AnnouncementCampusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementCampusTypeRepository extends JpaRepository<AnnouncementCampusType, Integer> {
    List<AnnouncementCampusType> findByAnnouncementId(Integer announcementId);
    void deleteByAnnouncementId(Integer announcementId);

    @Query("SELECT act.campusTypeId FROM AnnouncementCampusType act WHERE act.announcementId = :announcementId")
    List<Integer> findCampusTypeIdsByAnnouncementId(@Param("announcementId") Integer announcementId);
}
