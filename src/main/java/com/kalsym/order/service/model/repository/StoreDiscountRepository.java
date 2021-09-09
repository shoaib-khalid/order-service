package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.StoreDiscount;
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
public interface StoreDiscountRepository extends PagingAndSortingRepository<StoreDiscount, String>, JpaRepository<StoreDiscount, String> {
    
    @Query("SELECT m FROM StoreDiscount m WHERE m.storeId = :queryStoreId AND m.isActive=true AND m.startDate < :currentDate AND m.endDate > :currentDate") 
    List<StoreDiscount> findAvailableDiscount(
            @Param("queryStoreId") String storeId,
            @Param("currentDate") Date currentDate
            );
}
