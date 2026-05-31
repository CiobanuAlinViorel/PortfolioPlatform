package com.example.portofolio.service.base;

import com.example.portofolio.repository.base.RepositoryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Slf4j
public abstract class BaseService<T, ID, R extends JpaRepository<T, ID>> {

    protected final R repository;

    protected BaseService(R repository) {
        this.repository = repository;
    }

    // ===== BASIC READ OPERATIONS =====

    /**
     * Find all entities
     */
    public List<T> findAll() {
        return repository.findAll();
    }

    /**
     * Find all entities with pagination
     */
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * Find all entities with default pagination
     */
    public Page<T> findAll(int page, int size) {
        RepositoryUtils.validatePageParameters(page, size);
        Pageable pageable = RepositoryUtils.createPageable(page, size);
        return repository.findAll(pageable);
    }

    /**
     * Check if entity exists
     */
    public boolean exists(ID id) {
        return repository.existsById(id);
    }

    /**
     * Count total entities
     */
    public long count() {
        return repository.count();
    }

    // ===== SEARCH & FILTERING (using existing repository methods) =====

    /**
     * Search entities by term (override in specific services)
     */
    public List<T> search() {
        // Base implementation - override in specific services
        return findAll();
    }




    // ===== METADATA SUPPORT (optional - override in services that use EntityMetadata) =====

    /**
     * Find featured entities (override in services with metadata)
     */
    public List<T> findFeatured() {
        // Override in services that have EntityMetadata relationship
        return List.of();
    }


    // ===== UTILITY METHODS =====

    /**
     * Refresh/reload entity from database
     */
    public Optional<T> refresh(ID id) {
        return repository.findById(id);
    }

    // ===== TEMPLATE METHODS =====

    
    /**
     * Transform entity to DTO - must be implemented by subclasses
     */
    protected abstract Object toDto(T entity);


}
