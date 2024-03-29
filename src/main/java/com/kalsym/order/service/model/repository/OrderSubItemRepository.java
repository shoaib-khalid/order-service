package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.OrderSubItem;
import java.util.List;
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
public interface OrderSubItemRepository extends PagingAndSortingRepository<OrderSubItem, String>, JpaRepository<OrderSubItem, String> {

    <S extends Object> Page<S> findByOrderItemId(@Param("orderItemId") String orderItemId,  Pageable pgbl);
    
    List<OrderSubItem> findByOrderItemId(@Param("orderItemId") String orderItemId);
    
    //OrderItem findByOrderIdAndProductId(@Param("orderId") String orderId, @Param("productId") String productId);
}
