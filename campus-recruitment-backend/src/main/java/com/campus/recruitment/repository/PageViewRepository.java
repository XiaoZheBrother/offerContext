package com.campus.recruitment.repository;

import com.campus.recruitment.entity.PageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PageViewRepository extends JpaRepository<PageView, Long> {

    @Query("SELECT COUNT(pv) FROM PageView pv WHERE DATE(pv.visitTime) = CURRENT_DATE")
    long countTodayPv();

    @Query("SELECT COUNT(DISTINCT pv.visitorId) FROM PageView pv WHERE DATE(pv.visitTime) = CURRENT_DATE")
    long countTodayUv();
}
