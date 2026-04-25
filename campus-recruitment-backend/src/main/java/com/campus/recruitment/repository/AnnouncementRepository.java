package com.campus.recruitment.repository;

import com.campus.recruitment.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Integer>, JpaSpecificationExecutor<Announcement> {

    long countByOnlineStatus(Short onlineStatus);

    @Query("SELECT a FROM Announcement a WHERE a.companyId = :companyId AND a.onlineStatus = 1")
    List<Announcement> findOnlineByCompanyId(@Param("companyId") Integer companyId);

    @Query("SELECT MAX(a.announcementId) FROM Announcement a")
    Optional<Integer> findMaxId();
}
