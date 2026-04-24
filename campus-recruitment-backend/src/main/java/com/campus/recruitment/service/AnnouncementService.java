package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResponse;
import com.campus.recruitment.dto.ApplyStatus;
import com.campus.recruitment.dto.request.AnnouncementQueryRequest;
import com.campus.recruitment.dto.response.AnnouncementDetailResponse;
import com.campus.recruitment.dto.response.AnnouncementListResponse;
import com.campus.recruitment.entity.*;
import com.campus.recruitment.exception.ResourceNotFoundException;
import com.campus.recruitment.repository.*;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final CompanyRepository companyRepository;
    private final CityRepository cityRepository;
    private final ClassTypeRepository classTypeRepository;
    private final CampusTypeRepository campusTypeRepository;
    private final DegreeRepository degreeRepository;
    private final IndustryTypeRepository industryTypeRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final AnnouncementCityRepository announcementCityRepository;
    private final AnnouncementClassTypeRepository announcementClassTypeRepository;
    private final AnnouncementCampusTypeRepository announcementCampusTypeRepository;
    private final AnnouncementDegreeRepository announcementDegreeRepository;
    private final AnnouncementIndustryTypeRepository announcementIndustryTypeRepository;
    private final AnnouncementJobCategoryRepository announcementJobCategoryRepository;
    private final CompanyDescriptionRepository companyDescriptionRepository;
    private final CompanyIndustryTypeRepository companyIndustryTypeRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               CompanyRepository companyRepository,
                               CityRepository cityRepository,
                               ClassTypeRepository classTypeRepository,
                               CampusTypeRepository campusTypeRepository,
                               DegreeRepository degreeRepository,
                               IndustryTypeRepository industryTypeRepository,
                               JobCategoryRepository jobCategoryRepository,
                               AnnouncementCityRepository announcementCityRepository,
                               AnnouncementClassTypeRepository announcementClassTypeRepository,
                               AnnouncementCampusTypeRepository announcementCampusTypeRepository,
                               AnnouncementDegreeRepository announcementDegreeRepository,
                               AnnouncementIndustryTypeRepository announcementIndustryTypeRepository,
                               AnnouncementJobCategoryRepository announcementJobCategoryRepository,
                               CompanyDescriptionRepository companyDescriptionRepository,
                               CompanyIndustryTypeRepository companyIndustryTypeRepository) {
        this.announcementRepository = announcementRepository;
        this.companyRepository = companyRepository;
        this.cityRepository = cityRepository;
        this.classTypeRepository = classTypeRepository;
        this.campusTypeRepository = campusTypeRepository;
        this.degreeRepository = degreeRepository;
        this.industryTypeRepository = industryTypeRepository;
        this.jobCategoryRepository = jobCategoryRepository;
        this.announcementCityRepository = announcementCityRepository;
        this.announcementClassTypeRepository = announcementClassTypeRepository;
        this.announcementCampusTypeRepository = announcementCampusTypeRepository;
        this.announcementDegreeRepository = announcementDegreeRepository;
        this.announcementIndustryTypeRepository = announcementIndustryTypeRepository;
        this.announcementJobCategoryRepository = announcementJobCategoryRepository;
        this.companyDescriptionRepository = companyDescriptionRepository;
        this.companyIndustryTypeRepository = companyIndustryTypeRepository;
    }

    public PageResponse<AnnouncementListResponse> getAnnouncementList(AnnouncementQueryRequest request) {
        Specification<Announcement> spec = buildSpecification(request);

        // Sort: not-expired first, then by created_at DESC
        // CASE WHEN (expired_at IS NULL OR expired_at > NOW()) THEN 0 ELSE 1 END ASC, created_at DESC
        Sort sort = Sort.by(
                Sort.Order.asc("expiredAt"),  // Will be overridden by native sort expression below
                Sort.Order.desc("createdAt")
        );

        // Use JPA Specification with custom sort expression
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getPageSize());

        Page<Announcement> page = announcementRepository.findAll(spec, pageable);

        // Build lookup maps for batch resolution
        Map<Integer, String> cityMap = cityRepository.findAll().stream()
                .collect(Collectors.toMap(City::getCityId, City::getName));
        Map<Integer, String> classTypeMap = classTypeRepository.findAll().stream()
                .collect(Collectors.toMap(ClassType::getClassTypeId, ClassType::getName));
        Map<Integer, String> campusTypeMap = campusTypeRepository.findAll().stream()
                .collect(Collectors.toMap(CampusType::getCampusTypeId, CampusType::getName));
        Map<Integer, String> companyMap = companyRepository.findAll().stream()
                .collect(Collectors.toMap(Company::getCompanyId, Company::getName));

        List<AnnouncementListResponse> list = page.getContent().stream()
                .map(announcement -> {
                    AnnouncementListResponse dto = new AnnouncementListResponse();
                    dto.setAnnouncementId(announcement.getAnnouncementId());
                    dto.setCompanyName(companyMap.getOrDefault(announcement.getCompanyId(), ""));
                    dto.setName(announcement.getName());

                    // Batch fetch related names from join tables
                    dto.setCityNames(fetchCityNames(announcement.getAnnouncementId(), cityMap));
                    dto.setClassTypeNames(fetchClassTypeNames(announcement.getAnnouncementId(), classTypeMap));
                    dto.setCampusTypeNames(fetchCampusTypeNames(announcement.getAnnouncementId(), campusTypeMap));

                    // Calculate apply status
                    dto.setApplyStatus(ApplyStatus.calculate(announcement.getPublishedAt(), announcement.getExpiredAt()));

                    // Set expiredAt - use publishedAt + 90 days as default if null
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

        return PageResponse.of(list, page.getTotalElements(), request.getPage(), request.getPageSize());
    }

    public AnnouncementDetailResponse getAnnouncementDetail(Integer id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));

        if (announcement.getOnlineStatus() == null || announcement.getOnlineStatus() != 1) {
            throw new ResourceNotFoundException("Announcement not found with id: " + id);
        }

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

        // Published / expired dates
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

        // Apply status
        response.setApplyStatus(ApplyStatus.calculate(announcement.getPublishedAt(), announcement.getExpiredAt()));

        // Company
        if (announcement.getCompanyId() != null) {
            companyRepository.findById(announcement.getCompanyId()).ifPresent(company -> {
                response.setCompanyName(company.getName());

                // Company descriptions
                List<CompanyDescription> descriptions = companyDescriptionRepository.findByCompanyId(company.getCompanyId());
                response.setCompanyDescriptions(descriptions.stream()
                        .map(CompanyDescription::getDescription)
                        .collect(Collectors.toList()));

                // Company industry types
                List<CompanyIndustryType> companyIndustryTypes = companyIndustryTypeRepository.findByCompanyId(company.getCompanyId());
                Map<Integer, String> industryTypeMap = industryTypeRepository.findAll().stream()
                        .collect(Collectors.toMap(IndustryType::getIndustryTypeId, IndustryType::getName));
                response.setCompanyIndustryNames(companyIndustryTypes.stream()
                        .map(cit -> industryTypeMap.getOrDefault(cit.getIndustryTypeId(), ""))
                        .filter(name -> !name.isEmpty())
                        .collect(Collectors.toList()));
            });
        }

        // Cities
        response.setCityNames(fetchCityNames(announcement.getAnnouncementId()));

        // ClassTypes
        response.setClassTypeNames(fetchClassTypeNames(announcement.getAnnouncementId()));

        // CampusTypes
        response.setCampusTypeNames(fetchCampusTypeNames(announcement.getAnnouncementId()));

        // Degrees
        response.setDegreeNames(fetchDegreeNames(announcement.getAnnouncementId()));

        // IndustryTypes
        response.setIndustryTypeNames(fetchIndustryTypeNames(announcement.getAnnouncementId()));

        // JobCategories
        response.setJobCategoryNames(fetchJobCategoryNames(announcement.getAnnouncementId()));

        return response;
    }

    private Specification<Announcement> buildSpecification(AnnouncementQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter onlineStatus = 1
            predicates.add(cb.equal(root.get("onlineStatus"), (short) 1));

            // keyword: join Company table, match company name LIKE %keyword%
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                Subquery<Integer> companySubquery = query.subquery(Integer.class);
                var companyRoot = companySubquery.from(Company.class);
                companySubquery.select(companyRoot.get("companyId"))
                        .where(cb.like(companyRoot.get("name"), "%" + request.getKeyword() + "%"));
                predicates.add(root.get("companyId").in(companySubquery));
            }

            // classTypeIds: EXISTS subquery in announcement_class_types
            if (request.getClassTypeIds() != null && !request.getClassTypeIds().isEmpty()) {
                Subquery<Integer> classTypeSubquery = query.subquery(Integer.class);
                var actRoot = classTypeSubquery.from(AnnouncementClassType.class);
                classTypeSubquery.select(actRoot.get("announcementId"))
                        .where(cb.and(
                                cb.equal(actRoot.get("announcementId"), root.get("announcementId")),
                                actRoot.get("classTypeId").in(request.getClassTypeIds())
                        ));
                predicates.add(cb.exists(classTypeSubquery));
            }

            // campusTypeIds: EXISTS subquery in announcement_campus_types
            if (request.getCampusTypeIds() != null && !request.getCampusTypeIds().isEmpty()) {
                Subquery<Integer> campusTypeSubquery = query.subquery(Integer.class);
                var actRoot = campusTypeSubquery.from(AnnouncementCampusType.class);
                campusTypeSubquery.select(actRoot.get("announcementId"))
                        .where(cb.and(
                                cb.equal(actRoot.get("announcementId"), root.get("announcementId")),
                                actRoot.get("campusTypeId").in(request.getCampusTypeIds())
                        ));
                predicates.add(cb.exists(campusTypeSubquery));
            }

            // cityIds: EXISTS subquery in announcement_cities
            if (request.getCityIds() != null && !request.getCityIds().isEmpty()) {
                Subquery<Integer> citySubquery = query.subquery(Integer.class);
                var acRoot = citySubquery.from(AnnouncementCity.class);
                citySubquery.select(acRoot.get("announcementId"))
                        .where(cb.and(
                                cb.equal(acRoot.get("announcementId"), root.get("announcementId")),
                                acRoot.get("cityId").in(request.getCityIds())
                        ));
                predicates.add(cb.exists(citySubquery));
            }

            // applyStatus filter
            LocalDateTime now = LocalDateTime.now();
            if ("ongoing".equalsIgnoreCase(request.getApplyStatus())) {
                // published_at <= now AND (expired_at IS NULL OR expired_at > now)
                predicates.add(cb.lessThanOrEqualTo(root.get("publishedAt"), now));
                predicates.add(cb.or(
                        cb.isNull(root.get("expiredAt")),
                        cb.greaterThan(root.get("expiredAt"), now)
                ));
            } else if ("expired".equalsIgnoreCase(request.getApplyStatus())) {
                // expired_at IS NOT NULL AND expired_at <= now
                predicates.add(cb.isNotNull(root.get("expiredAt")));
                predicates.add(cb.lessThanOrEqualTo(root.get("expiredAt"), now));
            }

            // Sort: not-expired first (0), expired (1), then by created_at DESC
            // CASE WHEN (expired_at IS NULL OR expired_at > NOW()) THEN 0 ELSE 1 END
            jakarta.persistence.criteria.Expression<Integer> expiredOrder = cb.<Integer>selectCase()
                    .when(cb.or(
                            cb.isNull(root.get("expiredAt")),
                            cb.greaterThan(root.get("expiredAt"), now)
                    ), 0)
                    .otherwise(1);

            query.orderBy(cb.asc(expiredOrder), cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<String> fetchCityNames(Integer announcementId, Map<Integer, String> cityMap) {
        List<Integer> cityIds = announcementCityRepository.findCityIdsByAnnouncementId(announcementId);
        return cityIds.stream()
                .map(id -> cityMap.getOrDefault(id, ""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> fetchClassTypeNames(Integer announcementId, Map<Integer, String> classTypeMap) {
        List<Integer> classTypeIds = announcementClassTypeRepository.findClassTypeIdsByAnnouncementId(announcementId);
        return classTypeIds.stream()
                .map(id -> classTypeMap.getOrDefault(id, ""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> fetchCampusTypeNames(Integer announcementId, Map<Integer, String> campusTypeMap) {
        List<Integer> campusTypeIds = announcementCampusTypeRepository.findCampusTypeIdsByAnnouncementId(announcementId);
        return campusTypeIds.stream()
                .map(id -> campusTypeMap.getOrDefault(id, ""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> fetchCityNames(Integer announcementId) {
        List<Integer> cityIds = announcementCityRepository.findCityIdsByAnnouncementId(announcementId);
        Map<Integer, String> cityMap = cityRepository.findAll().stream()
                .collect(Collectors.toMap(City::getCityId, City::getName));
        return cityIds.stream()
                .map(id -> cityMap.getOrDefault(id, ""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> fetchClassTypeNames(Integer announcementId) {
        List<Integer> classTypeIds = announcementClassTypeRepository.findClassTypeIdsByAnnouncementId(announcementId);
        Map<Integer, String> classTypeMap = classTypeRepository.findAll().stream()
                .collect(Collectors.toMap(ClassType::getClassTypeId, ClassType::getName));
        return classTypeIds.stream()
                .map(id -> classTypeMap.getOrDefault(id, ""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> fetchCampusTypeNames(Integer announcementId) {
        List<Integer> campusTypeIds = announcementCampusTypeRepository.findCampusTypeIdsByAnnouncementId(announcementId);
        Map<Integer, String> campusTypeMap = campusTypeRepository.findAll().stream()
                .collect(Collectors.toMap(CampusType::getCampusTypeId, CampusType::getName));
        return campusTypeIds.stream()
                .map(id -> campusTypeMap.getOrDefault(id, ""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> fetchDegreeNames(Integer announcementId) {
        List<AnnouncementDegree> degrees = announcementDegreeRepository.findByAnnouncementId(announcementId);
        Map<Integer, String> degreeMap = degreeRepository.findAll().stream()
                .collect(Collectors.toMap(Degree::getDegreeId, Degree::getName));
        return degrees.stream()
                .map(ad -> degreeMap.getOrDefault(ad.getDegreeId(), ""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> fetchIndustryTypeNames(Integer announcementId) {
        List<AnnouncementIndustryType> industryTypes = announcementIndustryTypeRepository.findByAnnouncementId(announcementId);
        Map<Integer, String> industryTypeMap = industryTypeRepository.findAll().stream()
                .collect(Collectors.toMap(IndustryType::getIndustryTypeId, IndustryType::getName));
        return industryTypes.stream()
                .map(ait -> industryTypeMap.getOrDefault(ait.getIndustryTypeId(), ""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> fetchJobCategoryNames(Integer announcementId) {
        List<AnnouncementJobCategory> jobCategories = announcementJobCategoryRepository.findByAnnouncementId(announcementId);
        Map<Integer, String> jobCategoryMap = jobCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(JobCategory::getJobCategoryId, JobCategory::getName));
        return jobCategories.stream()
                .map(ajc -> jobCategoryMap.getOrDefault(ajc.getJobCategoryId(), ""))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());
    }
}
