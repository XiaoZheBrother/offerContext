package com.campus.recruitment.repository;

import com.campus.recruitment.entity.AnnouncementCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementCityRepository extends JpaRepository<AnnouncementCity, Integer> {
    List<AnnouncementCity> findByAnnouncementId(Integer announcementId);
    void deleteByAnnouncementId(Integer announcementId);

    @Query("SELECT ac.cityId FROM AnnouncementCity ac WHERE ac.announcementId = :announcementId")
    List<Integer> findCityIdsByAnnouncementId(@Param("announcementId") Integer announcementId);
}
