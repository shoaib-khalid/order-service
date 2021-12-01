package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.ProductInventory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author 7cu
 */
@Repository
public interface ProductInventoryRepository extends PagingAndSortingRepository<ProductInventory, String>, JpaRepository<ProductInventory, String> {

    List<ProductInventory> findByProductId(@Param("productId") String productId);
    
    ProductInventory findByItemCode(@Param("itemCode") String itemCode);
}
