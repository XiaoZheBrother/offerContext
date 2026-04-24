package com.campus.recruitment.repository;

import com.campus.recruitment.entity.ClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {

    @Query("SELECT COUNT(cl) FROM ClickLog cl WHERE DATE(cl.clickTime) = CURRENT_DATE")
    long countTodayClicks();

    @Query(value = "SELECT cl.company_id, c.name, COUNT(*) as click_count " +
           "FROM click_logs cl JOIN companies c ON cl.company_id = c.company_id " +
           "WHERE cl.click_time >= :startTime " +
           "GROUP BY cl.company_id, c.name " +
           "ORDER BY click_count DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopCompaniesByClickTimeAfter(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);

    @Query(value = "SELECT cl.company_id, c.name, COUNT(*) as click_count " +
           "FROM click_logs cl JOIN companies c ON cl.company_id = c.company_id " +
           "GROUP BY cl.company_id, c.name " +
           "ORDER BY click_count DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopCompaniesAllTime(@Param("limit") int limit);
}
