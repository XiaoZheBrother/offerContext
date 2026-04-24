package com.campus.recruitment.repository;

import com.campus.recruitment.entity.Degree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DegreeRepository extends JpaRepository<Degree, Integer> {
}
