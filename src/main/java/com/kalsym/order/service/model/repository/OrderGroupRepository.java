package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderGroup;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
public interface OrderGroupRepository extends PagingAndSortingRepository<OrderGroup, String>, JpaRepository<OrderGroup, String> , JpaSpecificationExecutor<OrderGroup> {

    @Transactional 
    @Modifying
    @Query("UPDATE OrderGroup m SET m.orderQrGroupId=:qrOrderGroupId WHERE m.id = :orderGroupId") 
    void UpdateQrcodeOrderGroupId(
            @Param("qrOrderGroupId") long qrOrderGroupId,
            @Param("orderGroupId") String orderGroupId            
            );
}
