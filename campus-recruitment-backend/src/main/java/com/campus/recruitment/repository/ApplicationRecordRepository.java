package com.campus.recruitment.repository;

import com.campus.recruitment.entity.ApplicationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRecordRepository extends JpaRepository<ApplicationRecord, Long> {

    List<ApplicationRecord> findByUserIdOrderByAppliedAtDesc(Long userId);

    Optional<ApplicationRecord> findByUserIdAndAnnouncementId(Long userId, Integer announcementId);

    void deleteByUserIdAndAnnouncementId(Long userId, Integer announcementId);

    boolean existsByUserIdAndAnnouncementId(Long userId, Integer announcementId);
}
