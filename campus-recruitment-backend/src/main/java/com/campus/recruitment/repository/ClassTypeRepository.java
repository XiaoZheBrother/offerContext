package com.campus.recruitment.repository;

import com.campus.recruitment.entity.ClassType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassTypeRepository extends JpaRepository<ClassType, Integer> {
}
