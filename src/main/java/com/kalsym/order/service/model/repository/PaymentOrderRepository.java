package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.PaymentOrder;
import java.util.List;
import java.util.Optional;
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
public interface PaymentOrderRepository extends PagingAndSortingRepository<PaymentOrder, String>, JpaRepository<PaymentOrder, String> {

    Optional<PaymentOrder> findByClientTransactionId(@Param("orderId") String orderId);
}
