package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author 7cu
 */
@Repository
public interface OrderPaymentDetailRepository extends PagingAndSortingRepository<OrderPaymentDetail, String>, JpaRepository<OrderPaymentDetail, String> {

    <S extends Object> Page<S> findByOrderId(@Param("orderId") String orderId, Pageable pgbl);
    
    List<OrderPaymentDetail> findByDeliveryQuotationReferenceId(@Param("deliveryQuotationId") String deliveryQuotationId);
    
    OrderPaymentDetail findByOrderId(@Param("orderId") String orderId);
}
