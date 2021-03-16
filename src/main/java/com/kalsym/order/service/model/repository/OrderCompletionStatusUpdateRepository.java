package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
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
public interface OrderCompletionStatusUpdateRepository extends PagingAndSortingRepository<OrderCompletionStatusUpdate, String>, JpaRepository<OrderCompletionStatusUpdate, String> {

     <S extends Object> Page<S> findByOrderId(@Param("orderId") String orderId, Pageable pgbl);
}
