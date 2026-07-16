package com.example.portfolio.certificate.persistence;

import com.example.portfolio.certificate.domain.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long>, JpaSpecificationExecutor<Certificate> {
    List<Certificate> findByProfileId(Long profileId);
    List<Certificate> findByCertificationCategoryId(Long categoryId);
}
