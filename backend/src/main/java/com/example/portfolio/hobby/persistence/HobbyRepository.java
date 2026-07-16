package com.example.portfolio.hobby.persistence;

import com.example.portfolio.hobby.domain.Hobby;
import com.example.portfolio.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface HobbyRepository extends JpaRepository<Hobby, Long>, JpaSpecificationExecutor<Hobby> {
    List<Hobby> findByProfileOrderByNameAsc(Profile profile);
}
