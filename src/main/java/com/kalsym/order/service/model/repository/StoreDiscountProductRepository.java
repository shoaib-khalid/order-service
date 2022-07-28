package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.StoreDiscountProduct;
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
public interface StoreDiscountProductRepository extends PagingAndSortingRepository<StoreDiscountProduct, String>, JpaRepository<StoreDiscountProduct, String> {
  
    List<StoreDiscountProduct> findByStoreDiscountId(@Param("storeDiscountId") String storeDiscountId);
        
}
