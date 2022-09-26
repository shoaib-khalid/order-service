package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.CartItemAddOn;
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
public interface CartItemAddOnRepository extends PagingAndSortingRepository<CartItemAddOn, String>, JpaRepository<CartItemAddOn, String> {

    
}
