package com.campus.recruitment.repository;

import com.campus.recruitment.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
    Optional<City> findByName(String name);
    List<City> findByIsTopOrderByWeightDescNameAsc(Short isTop);
    List<City> findAllByOrderByIsTopDescWeightDescNameAsc();

    @Query("SELECT MAX(c.cityId) FROM City c")
    Optional<Integer> findMaxId();
}
