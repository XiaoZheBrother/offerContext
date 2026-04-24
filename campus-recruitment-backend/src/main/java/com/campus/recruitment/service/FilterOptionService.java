package com.campus.recruitment.service;

import com.campus.recruitment.dto.response.FilterOptionsResponse;
import com.campus.recruitment.dto.response.FilterOptionsResponse.FilterItem;
import com.campus.recruitment.entity.CampusType;
import com.campus.recruitment.entity.City;
import com.campus.recruitment.entity.ClassType;
import com.campus.recruitment.repository.CampusTypeRepository;
import com.campus.recruitment.repository.CityRepository;
import com.campus.recruitment.repository.ClassTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FilterOptionService {

    private final ClassTypeRepository classTypeRepository;
    private final CampusTypeRepository campusTypeRepository;
    private final CityRepository cityRepository;

    private final ConcurrentHashMap<String, CachedFilterOptions> cache = new ConcurrentHashMap<>();
    private static final long TTL_MS = 60 * 60 * 1000; // 1 hour

    public FilterOptionService(ClassTypeRepository classTypeRepository,
                               CampusTypeRepository campusTypeRepository,
                               CityRepository cityRepository) {
        this.classTypeRepository = classTypeRepository;
        this.campusTypeRepository = campusTypeRepository;
        this.cityRepository = cityRepository;
    }

    public FilterOptionsResponse getFilterOptions() {
        CachedFilterOptions cached = cache.get("filterOptions");
        if (cached != null && !cached.isExpired()) {
            return cached.getData();
        }

        FilterOptionsResponse response = buildFilterOptions();
        cache.put("filterOptions", new CachedFilterOptions(response));
        return response;
    }

    public void evictCache() {
        cache.remove("filterOptions");
    }

    private FilterOptionsResponse buildFilterOptions() {
        FilterOptionsResponse response = new FilterOptionsResponse();

        // ClassTypes
        List<ClassType> classTypes = classTypeRepository.findAll();
        response.setClassTypes(classTypes.stream()
                .map(ct -> new FilterItem(ct.getClassTypeId(), ct.getName()))
                .collect(Collectors.toList()));

        // CampusTypes
        List<CampusType> campusTypes = campusTypeRepository.findAll();
        response.setCampusTypes(campusTypes.stream()
                .map(ct -> new FilterItem(ct.getCampusTypeId(), ct.getName()))
                .collect(Collectors.toList()));

        // Cities: sorted by isTop DESC, weight DESC, name ASC
        List<City> cities = cityRepository.findAllByOrderByIsTopDescWeightDescNameAsc();
        response.setCities(cities.stream()
                .map(city -> new FilterItem(
                        city.getCityId(),
                        city.getName(),
                        city.getIsTop() != null && city.getIsTop() == 1
                ))
                .collect(Collectors.toList()));

        return response;
    }

    private static class CachedFilterOptions {
        private final FilterOptionsResponse data;
        private final long createdAt;

        CachedFilterOptions(FilterOptionsResponse data) {
            this.data = data;
            this.createdAt = System.currentTimeMillis();
        }

        FilterOptionsResponse getData() {
            return data;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > TTL_MS;
        }
    }
}
