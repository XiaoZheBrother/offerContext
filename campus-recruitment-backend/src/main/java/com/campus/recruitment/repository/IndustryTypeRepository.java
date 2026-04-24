package com.campus.recruitment.repository;

import com.campus.recruitment.entity.IndustryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndustryTypeRepository extends JpaRepository<IndustryType, Integer> {
}
