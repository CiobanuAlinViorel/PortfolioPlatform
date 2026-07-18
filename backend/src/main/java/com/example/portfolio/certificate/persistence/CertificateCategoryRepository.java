package com.example.portfolio.certificate.persistence;

import com.example.portfolio.certificate.domain.CertificationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateCategoryRepository extends JpaRepository<CertificationCategory, Long> {
    Optional<CertificationCategory> findByName(String name);
}
