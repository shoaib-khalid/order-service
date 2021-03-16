package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.OrderPaymentStatusUpdate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author 7cu
 */
@Repository
public interface OrderPaymentStatusUpdateRepository extends PagingAndSortingRepository<OrderPaymentStatusUpdate, String>, JpaRepository<OrderPaymentStatusUpdate, String> {

    <S extends Object> Page<S> findByOrderId(@Param("orderId") String orderId, Pageable pgbl);
}
