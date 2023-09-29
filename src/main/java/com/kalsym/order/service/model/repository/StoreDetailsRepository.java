package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.StoreWithDetails;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author 7cu
 */
@Repository
public interface StoreDetailsRepository extends PagingAndSortingRepository<StoreWithDetails, String>, JpaRepository<StoreWithDetails, String> {

//    List<Order> findByCustomerId(@Param("customerId") String customerId);

//    create a function to get the store Details from storeId



}
