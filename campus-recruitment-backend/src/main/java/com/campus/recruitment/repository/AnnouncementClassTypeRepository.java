package com.campus.recruitment.repository;

import com.campus.recruitment.entity.AnnouncementClassType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementClassTypeRepository extends JpaRepository<AnnouncementClassType, Integer> {
    List<AnnouncementClassType> findByAnnouncementId(Integer announcementId);
    void deleteByAnnouncementId(Integer announcementId);

    @Query("SELECT act.classTypeId FROM AnnouncementClassType act WHERE act.announcementId = :announcementId")
    List<Integer> findClassTypeIdsByAnnouncementId(@Param("announcementId") Integer announcementId);
}
