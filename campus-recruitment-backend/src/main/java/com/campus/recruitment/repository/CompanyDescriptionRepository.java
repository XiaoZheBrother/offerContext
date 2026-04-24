package com.campus.recruitment.repository;

import com.campus.recruitment.entity.CompanyDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyDescriptionRepository extends JpaRepository<CompanyDescription, Integer> {
    List<CompanyDescription> findByCompanyId(Integer companyId);
}
