package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.OrderItemSnapshot;
import java.util.List;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author 7cu
 */
@Repository
public interface OrderItemSnapshotRepository extends PagingAndSortingRepository<OrderItemSnapshot, String>, JpaRepository<OrderItemSnapshot, String> {

}
