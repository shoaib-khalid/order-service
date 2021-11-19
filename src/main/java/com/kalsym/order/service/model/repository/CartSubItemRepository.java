package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.CartSubItem;
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
public interface CartSubItemRepository extends PagingAndSortingRepository<CartSubItem, String>, JpaRepository<CartSubItem, String> {

    Page<CartSubItem> findByCartItemId(@Param("cartItemId") String cartItemId,  Pageable pgbl);
    
    CartSubItem findByCartItemIdAndProductId(@Param("cartItemId") String cartItemId, @Param("productId") String productId);
    
    CartSubItem findByCartItemIdAndItemCodeAndSpecialInstruction(@Param("cartItemId") String cartId, @Param("itemCode") String itemCode, @Param("specialInstruction") String specialInstruction);
    
    List<CartSubItem> findByCartItemId(@Param("cartItemId") String cartItemId);
    
    /**
     * clear cart item
     * @param queryCartId
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM CartSubItem m WHERE m.cartItemId = :queryCartItemId") 
    public void clearCartSubItem(
            @Param("queryCartItemId") String queryCartItemId
    );
}
