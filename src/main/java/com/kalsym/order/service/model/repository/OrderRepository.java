package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.model.Order;
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
public interface OrderRepository extends PagingAndSortingRepository<Order, String>, JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {

    @Query("SELECT c.completionStatus, COUNT(c.id) FROM Order AS c WHERE c.storeId = :storeId GROUP BY c.completionStatus")
    List<Object[]> getCountSummary(@Param("storeId") String storeId);
    
    @Query(value = "SELECT A.id, A.invoiceId, B.phoneNumber, B.name, B.clientId,  C.username, C.password  "
            + "FROM `order` A INNER JOIN `store` B ON A.storeId=B.id  "
            + "INNER JOIN `client` C ON B.clientId=C.id "
            + "WHERE completionStatus='PAYMENT_CONFIRMED' AND verticalCode='FNB' "
            + "AND DATE_ADD(A.created, INTERVAL 5 MINUTE) < NOW()", nativeQuery = true)
    List<Object[]> getFnBNotProcessOrder();
    
    @Transactional 
    @Modifying
    @Query("UPDATE Order m SET m.beingProcess=1 WHERE m.id = :orderId") 
    void UpdateOrderBeingProcess(
            @Param("orderId") String orderId
            );
    
    @Transactional 
    @Modifying
    @Query("UPDATE Order m SET m.beingProcess=0 WHERE m.id = :orderId") 
    void UpdateOrderFinishProcess(
            @Param("orderId") String orderId
            );
    
    
    @Transactional 
    @Modifying
    @Query("UPDATE Order m SET "
            + "m.completionStatus = :newStatus, "
            + "m.updated = :updatedTime "
            + "WHERE m.id = :orderId") 
    void CancelOrder(
            @Param("orderId") String orderId,
            @Param("newStatus") OrderStatus newStatus,
            @Param("updatedTime") Date updatedTime            
            );
}
