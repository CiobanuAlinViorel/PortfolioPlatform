package com.example.portofolio.service.personal;

import com.example.portofolio.dto.*;
import com.example.portofolio.entity.*;
import com.example.portofolio.entity.enums.*;
import com.example.portofolio.repository.*;
import com.example.portofolio.service.base.BaseService;
import com.example.portofolio.service.base.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

/**
 * Contact Service with ServiceUtils
 */
@Service
@Slf4j
public class ContactService extends BaseService<ContactInfo, Long, ContactInfoRepository> {

    private final ContactLocationRepository contactLocationRepository;

    @Autowired
    public ContactService(ContactInfoRepository contactInfoRepository,
                          ContactLocationRepository contactLocationRepository) {
        super(contactInfoRepository);
        this.contactLocationRepository = contactLocationRepository;
    }


    @Override
    protected ContactInfoDto toDto(ContactInfo contactInfo) {
        return toContactInfoDto(contactInfo);
    }

    // ===== CORE CONTACT QUERIES  =====

    @Cacheable(value = "contactByPersonal", key = "#personalId")
    public Optional<ContactInfoDto> findByPersonalId(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("findByPersonalId", personalId);
        ServiceUtils.validatePersonalId(personalId);

        Optional<ContactInfo> contactInfo = repository.findByPersonalId(personalId);
        Optional<ContactInfoDto> result = contactInfo.map(this::toContactInfoDto);

        ServiceUtils.logMethodExit("findByPersonalId", result.isPresent() ? 1 : 0);
        return result;
    }


    // ===== LOCATION METHODS =====

    public Optional<ContactLocationDto> findLocationByContactInfoId(@Valid @NotNull @Positive Long contactInfoId) {
        ServiceUtils.logMethodEntry("findLocationByContactInfoId", contactInfoId);
        ServiceUtils.validateEntityId(contactInfoId);

        Optional<ContactLocation> location = contactLocationRepository.findByContactInfoId(contactInfoId);
        Optional<ContactLocationDto> result = location.map(this::toContactLocationDto);

        ServiceUtils.logMethodExit("findLocationByContactInfoId", result.isPresent() ? 1 : 0);
        return result;
    }


    // ===== DTO CONVERSION =====

    private ContactInfoDto toContactInfoDto(ContactInfo contactInfo) {
        String location = null;
        if (contactInfo.getContactLocation() != null) {
            ContactLocation loc = contactInfo.getContactLocation();
            location = formatLocation(loc.getCity(), loc.getCountry());
        }

        return ContactInfoDto.builder()
                .email(contactInfo.getEmail())
                .phone(contactInfo.getPhone())
                .location(location)
                .github(contactInfo.getGithub())
                .linkedin(contactInfo.getLinkedin())
                .build();
    }

    private ContactLocationDto toContactLocationDto(ContactLocation location) {
        ContactLocationDto.CoordinatesDto coordinates = null;
        if (location.getLatitude() != null && location.getLongitude() != null) {
            coordinates = ContactLocationDto.CoordinatesDto.builder()
                    .lat(location.getLatitude().doubleValue())
                    .lng(location.getLongitude().doubleValue())
                    .build();
        }

        return ContactLocationDto.builder()
                .name(location.getName())
                .address(location.getAddress())
                .city(location.getCity())
                .country(location.getCountry())
                .coordinates(coordinates)
                .timezone(location.getTimezone())
                .workingHours(location.getWorkingHours())
                .build();
    }

    // ===== HELPER METHODS =====

    private String formatLocation(String city, String country) {
        if (city != null && country != null) {
            return city + ", " + country;
        }
        if (city != null) return city;
        return country;
    }


    // ===== METADATA SUPPORT =====

    @Override
    public List<ContactInfo> findFeatured() {
        // Contact info doesn't have featured concept
        return List.of();
    }

}


