package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.StoreDeliveryDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author 7cu
 */
@Repository
public interface StoreDeliveryDetailRepository extends PagingAndSortingRepository<StoreDeliveryDetail, String>, JpaRepository<StoreDeliveryDetail, String> {

    Optional<StoreDeliveryDetail> findByStoreId(@Param("storeId") String storeId);
    

}
