package com.campus.recruitment.repository;

import com.campus.recruitment.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Integer> {
    Optional<AdminUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
