package com.campus.recruitment.repository;

import com.campus.recruitment.entity.CompanyIndustryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyIndustryTypeRepository extends JpaRepository<CompanyIndustryType, Integer> {
    List<CompanyIndustryType> findByCompanyId(Integer companyId);
}
