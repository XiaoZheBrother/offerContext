package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResponse;
import com.campus.recruitment.dto.ApplyStatus;
import com.campus.recruitment.dto.request.AnnouncementCreateRequest;
import com.campus.recruitment.dto.response.AnnouncementDetailResponse;
import com.campus.recruitment.dto.response.AnnouncementListResponse;
import com.campus.recruitment.entity.*;
import com.campus.recruitment.exception.ResourceNotFoundException;
import com.campus.recruitment.repository.*;
import com.campus.recruitment.util.XssUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminAnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final CompanyRepository companyRepository;
    private final CityRepository cityRepository;
    private final ClassTypeRepository classTypeRepository;
    private final CampusTypeRepository campusTypeRepository;
    private final AnnouncementCityRepository announcementCityRepository;
    private final AnnouncementClassTypeRepository announcementClassTypeRepository;
    private final AnnouncementCampusTypeRepository announcementCampusTypeRepository;
    private final AnnouncementDegreeRepository announcementDegreeRepository;
    private final AnnouncementIndustryTypeRepository announcementIndustryTypeRepository;
    private final AnnouncementJobCategoryRepository announcementJobCategoryRepository;
    private final FilterOptionService filterOptionService;

    public AdminAnnouncementService(AnnouncementRepository announcementRepository,
                                    CompanyRepository companyRepository,
                                    CityRepository cityRepository,
                                    ClassTypeRepository classTypeRepository,
                                    CampusTypeRepository campusTypeRepository,
                                    AnnouncementCityRepository announcementCityRepository,
                                    AnnouncementClassTypeRepository announcementClassTypeRepository,
                                    AnnouncementCampusTypeRepository announcementCampusTypeRepository,
                                    AnnouncementDegreeRepository announcementDegreeRepository,
                                    AnnouncementIndustryTypeRepository announcementIndustryTypeRepository,
                                    AnnouncementJobCategoryRepository announcementJobCategoryRepository,
                                    FilterOptionService filterOptionService) {
        this.announcementRepository = announcementRepository;
        this.companyRepository = companyRepository;
        this.cityRepository = cityRepository;
        this.classTypeRepository = classTypeRepository;
        this.campusTypeRepository = campusTypeRepository;
        this.announcementCityRepository = announcementCityRepository;
        this.announcementClassTypeRepository = announcementClassTypeRepository;
        this.announcementCampusTypeRepository = announcementCampusTypeRepository;
        this.announcementDegreeRepository = announcementDegreeRepository;
        this.announcementIndustryTypeRepository = announcementIndustryTypeRepository;
        this.announcementJobCategoryRepository = announcementJobCategoryRepository;
        this.filterOptionService = filterOptionService;
    }

    @Transactional
    public int createAnnouncement(AnnouncementCreateRequest request) {
        // Find or create Company by name
        Company company = companyRepository.findByName(request.getCompanyName())
                .orElseGet(() -> {
                    Company newCompany = new Company();
                    newCompany.setName(request.getCompanyName());
                    newCompany.setCreatedAt(LocalDateTime.now());
                    newCompany.setUpdatedAt(LocalDateTime.now());
                    return companyRepository.save(newCompany);
                });

        // Handle custom city names: check if exists, if not create new City record
        if (request.getCustomCities() != null) {
            for (String cityName : request.getCustomCities()) {
                if (cityName != null && !cityName.isBlank()) {
                    if (!cityRepository.findByName(cityName).isPresent()) {
                        City newCity = new City();
                        newCity.setName(cityName);
                        newCity.setIsTop((short) 0);
                        newCity.setWeight(0);
                        newCity.setCreatedAt(LocalDateTime.now());
                        newCity.setUpdatedAt(LocalDateTime.now());
                        City savedCity = cityRepository.save(newCity);
                        // Add the new city ID to cityIds if not already present
                        if (!request.getCityIds().contains(savedCity.getCityId())) {
                            request.getCityIds().add(savedCity.getCityId());
                        }
                    }
                }
            }
        }

        int count = 0;
        LocalDateTime now = LocalDateTime.now();

        // For each campusTypeId, create a separate Announcement record (batch auto-split)
        for (Integer campusTypeId : request.getCampusTypeIds()) {
            Announcement announcement = new Announcement();
            announcement.setName(XssUtils.clean(request.getName()));
            announcement.setDetail(XssUtils.clean(request.getDetail()));
            announcement.setFromUrl(request.getApplyLink());  // applyLink -> from_url (投递链接)
            announcement.setLink(request.getLink());          // link = 宣发网址
            announcement.setCompanyId(company.getCompanyId());
            announcement.setOnlineStatus(request.getOnlineStatus() != null ? request.getOnlineStatus() : (short) 0);

            if (request.getPublishedAt() != null) {
                announcement.setPublishedAt(request.getPublishedAt().atStartOfDay());
            }
            if (request.getExpiredAt() != null) {
                announcement.setExpiredAt(request.getExpiredAt().atTime(23, 59, 59));
            }

            if (request.getCreatedAt() != null) {
                announcement.setCreatedAt(request.getCreatedAt().atStartOfDay());
            } else {
                announcement.setCreatedAt(now);
            }
            announcement.setUpdatedAt(now);

            Announcement saved = announcementRepository.save(announcement);

            // Save join table records - cities
            for (Integer cityId : request.getCityIds()) {
                AnnouncementCity ac = new AnnouncementCity();
                ac.setAnnouncementId(saved.getAnnouncementId());
                ac.setCityId(cityId);
                ac.setCreatedAt(now);
                ac.setUpdatedAt(now);
                announcementCityRepository.save(ac);
            }

            // Save join table records - classTypes
            for (Integer classTypeId : request.getClassTypeIds()) {
                AnnouncementClassType act = new AnnouncementClassType();
                act.setAnnouncementId(saved.getAnnouncementId());
                act.setClassTypeId(classTypeId);
                act.setCreatedAt(now);
                act.setUpdatedAt(now);
                announcementClassTypeRepository.save(act);
            }

            // Save join table record - campusType for current iteration
            AnnouncementCampusType act = new AnnouncementCampusType();
            act.setAnnouncementId(saved.getAnnouncementId());
            act.setCampusTypeId(campusTypeId);
            act.setCreatedAt(now);
            act.setUpdatedAt(now);
            announcementCampusTypeRepository.save(act);

            count++;
        }

        // Evict filter cache since new data was added
        filterOptionService.evictCache();

        return count;
    }

    @Transactional
    public void updateAnnouncement(Integer id, AnnouncementCreateRequest request) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));

        // Find or create Company by name
        Company company = companyRepository.findByName(request.getCompanyName())
                .orElseGet(() -> {
                    Company newCompany = new Company();
                    newCompany.setName(request.getCompanyName());
                    newCompany.setCreatedAt(LocalDateTime.now());
                    newCompany.setUpdatedAt(LocalDateTime.now());
                    return companyRepository.save(newCompany);
                });

        // Handle custom city names
        if (request.getCustomCities() != null) {
            for (String cityName : request.getCustomCities()) {
                if (cityName != null && !cityName.isBlank()) {
                    if (!cityRepository.findByName(cityName).isPresent()) {
                        City newCity = new City();
                        newCity.setName(cityName);
                        newCity.setIsTop((short) 0);
                        newCity.setWeight(0);
                        newCity.setCreatedAt(LocalDateTime.now());
                        newCity.setUpdatedAt(LocalDateTime.now());
                        City savedCity = cityRepository.save(newCity);
                        if (!request.getCityIds().contains(savedCity.getCityId())) {
                            request.getCityIds().add(savedCity.getCityId());
                        }
                    }
                }
            }
        }

        LocalDateTime now = LocalDateTime.now();

        // Update fields
        announcement.setName(XssUtils.clean(request.getName()));
        announcement.setDetail(XssUtils.clean(request.getDetail()));
        announcement.setFromUrl(request.getApplyLink());  // applyLink -> from_url (投递链接)
        announcement.setLink(request.getLink());          // link = 宣发网址
        announcement.setCompanyId(company.getCompanyId());

        if (request.getPublishedAt() != null) {
            announcement.setPublishedAt(request.getPublishedAt().atStartOfDay());
        }
        if (request.getExpiredAt() != null) {
            announcement.setExpiredAt(request.getExpiredAt().atTime(23, 59, 59));
        }
        if (request.getOnlineStatus() != null) {
            announcement.setOnlineStatus(request.getOnlineStatus());
        }

        // Keep original createdAt if provided, otherwise keep existing
        if (request.getCreatedAt() != null) {
            announcement.setCreatedAt(request.getCreatedAt().atStartOfDay());
        }
        announcement.setUpdatedAt(now);

        announcementRepository.save(announcement);

        // Delete and re-insert join table records
        announcementCityRepository.deleteByAnnouncementId(id);
        announcementClassTypeRepository.deleteByAnnouncementId(id);
        announcementCampusTypeRepository.deleteByAnnouncementId(id);

        // Re-insert cities
        for (Integer cityId : request.getCityIds()) {
            AnnouncementCity ac = new AnnouncementCity();
            ac.setAnnouncementId(id);
            ac.setCityId(cityId);
            ac.setCreatedAt(now);
            ac.setUpdatedAt(now);
            announcementCityRepository.save(ac);
        }

        // Re-insert classTypes
        for (Integer classTypeId : request.getClassTypeIds()) {
            AnnouncementClassType act = new AnnouncementClassType();
            act.setAnnouncementId(id);
            act.setClassTypeId(classTypeId);
            act.setCreatedAt(now);
            act.setUpdatedAt(now);
            announcementClassTypeRepository.save(act);
        }

        // Re-insert campusTypes (use first one for update, since batch-split is only for create)
        for (Integer campusTypeId : request.getCampusTypeIds()) {
            AnnouncementCampusType act = new AnnouncementCampusType();
            act.setAnnouncementId(id);
            act.setCampusTypeId(campusTypeId);
            act.setCreatedAt(now);
            act.setUpdatedAt(now);
            announcementCampusTypeRepository.save(act);
        }

        filterOptionService.evictCache();
    }

    @Transactional
    public void deleteAnnouncement(Integer id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));
        announcement.setOnlineStatus((short) 0);
        announcement.setUpdatedAt(LocalDateTime.now());
        announcementRepository.save(announcement);
        filterOptionService.evictCache();
    }

    @Transactional
    public void toggleOnlineStatus(Integer id, Short onlineStatus) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));
        announcement.setOnlineStatus(onlineStatus);
        announcement.setUpdatedAt(LocalDateTime.now());
        announcementRepository.save(announcement);
        filterOptionService.evictCache();
    }

    public PageResponse<AnnouncementListResponse> getAnnouncementsForAdmin(int page, int size, String keyword, String status) {
        Specification<Announcement> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // keyword filter on company name
            if (keyword != null && !keyword.isBlank()) {
                Subquery<Integer> companySubquery = query.subquery(Integer.class);
                var companyRoot = companySubquery.from(Company.class);
                companySubquery.select(companyRoot.get("companyId"))
                        .where(cb.like(companyRoot.get("name"), "%" + keyword + "%"));
                predicates.add(root.get("companyId").in(companySubquery));
            }

            // status filter
            if ("online".equalsIgnoreCase(status)) {
                predicates.add(cb.equal(root.get("onlineStatus"), (short) 1));
            } else if ("offline".equalsIgnoreCase(status)) {
                predicates.add(cb.equal(root.get("onlineStatus"), (short) 0));
            }

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Announcement> pageResult = announcementRepository.findAll(spec, pageable);

        // Build lookup maps
        Map<Integer, String> cityMap = cityRepository.findAll().stream()
                .collect(Collectors.toMap(City::getCityId, City::getName));
        Map<Integer, String> classTypeMap = classTypeRepository.findAll().stream()
                .collect(Collectors.toMap(ClassType::getClassTypeId, ClassType::getName));
        Map<Integer, String> campusTypeMap = campusTypeRepository.findAll().stream()
                .collect(Collectors.toMap(CampusType::getCampusTypeId, CampusType::getName));
        Map<Integer, String> companyMap = companyRepository.findAll().stream()
                .collect(Collectors.toMap(Company::getCompanyId, Company::getName));

        List<AnnouncementListResponse> list = pageResult.getContent().stream()
                .map(announcement -> {
                    AnnouncementListResponse dto = new AnnouncementListResponse();
                    dto.setAnnouncementId(announcement.getAnnouncementId());
                    dto.setCompanyName(companyMap.getOrDefault(announcement.getCompanyId(), ""));
                    dto.setName(announcement.getName());

                    // City names
                    List<Integer> cityIds = announcementCityRepository.findCityIdsByAnnouncementId(announcement.getAnnouncementId());
                    dto.setCityNames(cityIds.stream()
                            .map(cid -> cityMap.getOrDefault(cid, ""))
                            .filter(name -> !name.isEmpty())
                            .collect(Collectors.toList()));

                    // ClassType names
                    List<Integer> classTypeIds = announcementClassTypeRepository.findClassTypeIdsByAnnouncementId(announcement.getAnnouncementId());
                    dto.setClassTypeNames(classTypeIds.stream()
                            .map(ctid -> classTypeMap.getOrDefault(ctid, ""))
                            .filter(name -> !name.isEmpty())
                            .collect(Collectors.toList()));

                    // CampusType names
                    List<Integer> campusTypeIds = announcementCampusTypeRepository.findCampusTypeIdsByAnnouncementId(announcement.getAnnouncementId());
                    dto.setCampusTypeNames(campusTypeIds.stream()
                            .map(ctid -> campusTypeMap.getOrDefault(ctid, ""))
                            .filter(name -> !name.isEmpty())
                            .collect(Collectors.toList()));

                    // Apply status
                    dto.setApplyStatus(ApplyStatus.calculate(announcement.getPublishedAt(), announcement.getExpiredAt()));

                    // Online status
                    dto.setOnlineStatus(announcement.getOnlineStatus());

                    // ExpiredAt - use publishedAt + 90 days as default if null
                    if (announcement.getExpiredAt() != null) {
                        dto.setExpiredAt(announcement.getExpiredAt().toLocalDate());
                    } else if (announcement.getPublishedAt() != null) {
                        dto.setExpiredAt(announcement.getPublishedAt().plusDays(90).toLocalDate());
                    }

                    if (announcement.getCreatedAt() != null) {
                        dto.setCreatedAt(announcement.getCreatedAt().toLocalDate());
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        return PageResponse.of(list, pageResult.getTotalElements(), page, size);
    }

    public AnnouncementDetailResponse getAnnouncementDetail(Integer id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));

        AnnouncementDetailResponse response = new AnnouncementDetailResponse();
        response.setAnnouncementId(announcement.getAnnouncementId());
        response.setName(announcement.getName());
        response.setDetail(announcement.getDetail());
        response.setFromUrl(announcement.getFromUrl());
        response.setLink(announcement.getLink());
        response.setSalary(announcement.getSalary());
        response.setCompanyWelfare(announcement.getCompanyWelfare());
        response.setClassTime(announcement.getClassTime());
        response.setWrittenTest(announcement.getWrittenTest() != null && announcement.getWrittenTest() == 1);
        response.setAcceptWorkExperience(announcement.getAcceptWorkExperience() != null && announcement.getAcceptWorkExperience() == 1);

        if (announcement.getPublishedAt() != null) {
            response.setPublishedAt(announcement.getPublishedAt().toLocalDate());
        }
        if (announcement.getExpiredAt() != null) {
            response.setExpiredAt(announcement.getExpiredAt().toLocalDate());
        } else if (announcement.getPublishedAt() != null) {
            response.setExpiredAt(announcement.getPublishedAt().plusDays(90).toLocalDate());
        }
        if (announcement.getCreatedAt() != null) {
            response.setCreatedAt(announcement.getCreatedAt().toLocalDate());
        }

        response.setApplyStatus(ApplyStatus.calculate(announcement.getPublishedAt(), announcement.getExpiredAt()));

        // Company
        if (announcement.getCompanyId() != null) {
            companyRepository.findById(announcement.getCompanyId()).ifPresent(company -> {
                response.setCompanyName(company.getName());
            });
        }

        // Dimension IDs and names
        Map<Integer, String> cityMap = cityRepository.findAll().stream()
                .collect(Collectors.toMap(City::getCityId, City::getName));
        Map<Integer, String> classTypeMap = classTypeRepository.findAll().stream()
                .collect(Collectors.toMap(ClassType::getClassTypeId, ClassType::getName));
        Map<Integer, String> campusTypeMap = campusTypeRepository.findAll().stream()
                .collect(Collectors.toMap(CampusType::getCampusTypeId, CampusType::getName));

        List<Integer> cityIds = announcementCityRepository.findCityIdsByAnnouncementId(id);
        response.setCityIds(cityIds);
        response.setCityNames(cityIds.stream()
                .map(cid -> cityMap.getOrDefault(cid, ""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList()));

        List<Integer> classTypeIds = announcementClassTypeRepository.findClassTypeIdsByAnnouncementId(id);
        response.setClassTypeIds(classTypeIds);
        response.setClassTypeNames(classTypeIds.stream()
                .map(ctid -> classTypeMap.getOrDefault(ctid, ""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList()));

        List<Integer> campusTypeIds = announcementCampusTypeRepository.findCampusTypeIdsByAnnouncementId(id);
        response.setCampusTypeIds(campusTypeIds);
        response.setCampusTypeNames(campusTypeIds.stream()
                .map(ctid -> campusTypeMap.getOrDefault(ctid, ""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList()));

        return response;
    }
}
