package com.example.portfolio.certificate.mapper;

import com.example.portfolio.certificate.domain.Certificate;
import com.example.portfolio.certificate.domain.CertificationCategory;
import com.example.portfolio.certificate.dto.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CertificateMapper {

    @Mapping(source = "certificationCategory.id", target = "categoryId")
    @Mapping(source = "certificationCategory.name", target = "categoryName")
    CertificateDto toCertificateDto(Certificate certificate);

    CertificationCategoryDto toCategoryDto(CertificationCategory category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "certificationCategory", ignore = true)
    void updateCertificate(UpdateCertificateRequest request, @MappingTarget Certificate certificate);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "certificates", ignore = true)
    void updateCategory(UpdateCategoryRequest request, @MappingTarget CertificationCategory category);
}
