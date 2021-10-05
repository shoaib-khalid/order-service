package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

/**
 *
 * @author 7cu
 */
@Repository
public interface OrderRepository extends PagingAndSortingRepository<Order, String>, JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

//    List<Order> findByCustomerId(@Param("customerId") String customerId);
    
//    Page<Order> findAllByCreatedBetween(Date from, Date to, Pageable pageable);
    
    @Query("SELECT c.completionStatus, COUNT(c.id) FROM Order AS c GROUP BY c.completionStatus")
    List<Object[]> getCountSummary();
}
