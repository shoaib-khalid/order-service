package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.CartItem;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
public interface CartItemRepository extends PagingAndSortingRepository<CartItem, String>, JpaRepository<CartItem, String>, CustomRepository<CartItem, String> {

    Page<CartItem> findByCartId(@Param("cartId") String cartId,  Pageable pgbl);
    
    CartItem findByCartIdAndProductId(@Param("cartId") String cartId, @Param("productId") String productId);
    
    CartItem findByCartIdAndItemCodeAndSpecialInstruction(@Param("cartId") String cartId, @Param("itemCode") String itemCode, @Param("specialInstruction") String specialInstruction);
    
    List<CartItem> findByCartId(@Param("cartId") String cartId);
    
    List<CartItem> findByItemCode(@Param("itemCode") String itemCode);
    
    /**
     * clear cart item
     * @param queryCartId
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem m WHERE m.cartId = :queryCartId") 
    public void clearCartItem(
            @Param("queryCartId") String queryCartId
    );
    
    
    @Query(value = "SELECT A.id, itemCode, storeId FROM cart_item A INNER JOIN cart B ON A.cartId=B.id WHERE"
            + " isOpen=1 AND discountId IS NOT NULL "
            + "AND (DATE_ADD(discountCheckTimestamp, INTERVAL 1 HOUR) < NOW() OR discountCheckTimestamp IS NULL)", nativeQuery = true)
    List<Object[]> getCartItemWithDiscount();
    
}
