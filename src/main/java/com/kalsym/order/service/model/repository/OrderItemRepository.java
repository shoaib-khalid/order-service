package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.OrderItem;
import java.util.List;
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
public interface OrderItemRepository extends PagingAndSortingRepository<OrderItem, String>, JpaRepository<OrderItem, String> {

    <S extends Object> Page<S> findByOrderId(@Param("orderId") String orderId,  Pageable pgbl);
    
    List<OrderItem> findByOrderId(@Param("orderId") String orderId);
    
    //OrderItem findByOrderIdAndProductId(@Param("orderId") String orderId, @Param("productId") String productId);

    @Query(value = "SELECT COUNT(*) AS bil, A.itemCode, "
            + "D.id, D.name, D.seourl, D.categoryId, D.thumbnailUrl " +
        "FROM `order_item` A " +
        "	INNER JOIN `order` B ON A.orderId=B.id " +
        "	INNER JOIN `product_inventory` C ON A.itemCode=C.itemCode " +
        "	INNER JOIN `product` D ON C.productId=D.id " +
        "WHERE B.storeId=:storeId AND D.status='ACTIVE' " +
        "GROUP BY itemcode " +
        "ORDER BY bil DESC " +
        "LIMIT :limit", nativeQuery = true)
    List<Object[]> getFamousItemByStoreId(@Param("storeId") String storeId, int limit);
    
}
