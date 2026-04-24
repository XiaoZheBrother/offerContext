package com.campus.recruitment.repository;

import com.campus.recruitment.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
    Optional<Company> findByName(String name);
    List<Company> findByNameContaining(String keyword);
}
