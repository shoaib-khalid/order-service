package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author 7cu
 */
@Repository
public interface OrderShipmentDetailRepository extends PagingAndSortingRepository<OrderShipmentDetail, String>, JpaRepository<OrderShipmentDetail, String> {

    <S extends Object> Page<S> findByOrderId(@Param("orderId") String orderId, Pageable pgbl);
    
    OrderShipmentDetail findByOrderId(@Param("orderId") String orderId);
    
     @Transactional 
    @Modifying
    @Query("UPDATE OrderShipmentDetail m "
            + "SET m.customerTrackingUrl=:trackingUrl, "
            + "m.trackingNumber=:spOrderId "
            + "WHERE m.orderId = :orderId") 
    void UpdateTrackingUrlAndSpOrderId(
            @Param("trackingUrl") String trackingUrl,
            @Param("spOrderId") String spOrderId,
            @Param("orderId") String orderId
            );
}
