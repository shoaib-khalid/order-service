package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.model.OrderWithDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;
import java.util.Date;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author 7cu
 */
@Repository
public interface OrderWithDetailsRepository extends PagingAndSortingRepository<OrderWithDetails, String>, JpaRepository<OrderWithDetails, String>, JpaSpecificationExecutor<OrderWithDetails> {

}
