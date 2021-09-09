package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.StoreDiscountTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Date;

/**
 *
 * @author 7cu
 */
@Repository
public interface StoreDiscountTierRepository extends PagingAndSortingRepository<StoreDiscountTier, String>, JpaRepository<StoreDiscountTier, String> {
    
    @Query("SELECT m FROM StoreDiscountTier m WHERE m.storeDiscountId = :discountId AND m.startTotalSalesAmount <= :salesAmount AND m.endTotalSalesAmount >= :salesAmount") 
    StoreDiscountTier findDiscountTier(
            @Param("discountId") String discountId,
            @Param("salesAmount") Double salesAmount
            );
}
