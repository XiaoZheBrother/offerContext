package com.campus.recruitment.repository;

import com.campus.recruitment.entity.CampusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampusTypeRepository extends JpaRepository<CampusType, Integer> {
}
