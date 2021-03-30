package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.OrderCompletionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author 7cu
 */

@Repository
public interface OrderCompletionStatusRepository extends PagingAndSortingRepository<OrderCompletionStatus, String>, JpaRepository<OrderCompletionStatus, String> {

    <S extends Object> Page<S> findById(@Param("status") String status, Pageable pgbl);
}
