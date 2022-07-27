package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.Cart;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author 7cu
 */
@Repository
public interface CartRepository extends PagingAndSortingRepository<Cart, String>, JpaRepository<Cart, String> {

    List<Cart> findByCustomerId(@Param("customerId") String customerId);
     
    @Query(value = "SELECT A.id FROM `cart` A WHERE DATE_ADD(A.created, INTERVAL 24 HOUR) < NOW() " +
        "AND id NOT IN (SELECT cartId FROM cart_item);", nativeQuery = true)
    List<Object[]> findEmptyCart();
    
    List<Cart> findByCustomerIdAndStoreId(@Param("customerId") String customerId, @Param("storeId") String storeId);

}
