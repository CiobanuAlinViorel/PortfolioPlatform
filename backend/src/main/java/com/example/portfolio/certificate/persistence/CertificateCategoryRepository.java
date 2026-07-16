package com.example.portfolio.certificate.persistence;

import com.example.portfolio.certificate.domain.CertificationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateCategoryRepository extends JpaRepository<CertificationCategory, Long> {
}
