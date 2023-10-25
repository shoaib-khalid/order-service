package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.ProviderRatePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Sarosh
 */
@Repository
public interface ProviderRatePlanRepository extends JpaRepository<ProviderRatePlan, String> {
    Optional<ProviderRatePlan> findByIdProductCode(String productCode);

}
