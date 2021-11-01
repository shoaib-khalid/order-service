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

    @Query("SELECT c.completionStatus, COUNT(c.id) FROM Order AS c GROUP BY c.completionStatus")
    List<Object[]> getCountSummary();
    
    @Query(value = "SELECT A.id, A.invoiceId, B.phoneNumber, B.name  FROM `order` A INNER JOIN `store` B ON A.storeId=B.id  "
            + "WHERE completionStatus='PAYMENT_CONFIRMED' AND verticalCode='FNB' "
            + "AND DATE_ADD(A.created, INTERVAL 5 MINUTE) < NOW()", nativeQuery = true)
    List<Object[]> getFnBNotProcessOrder();
}
