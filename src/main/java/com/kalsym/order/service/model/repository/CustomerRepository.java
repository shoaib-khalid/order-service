/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.Customer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author taufik
 */
@Repository
public interface CustomerRepository extends PagingAndSortingRepository<Customer, String>, JpaRepository<Customer, String> {
        
    List<Customer> findByEmail(@Param("email") String email);
    
    List<Customer> findByEmailAndStoreId(@Param("email") String email, @Param("storeId") String storeId);
    
    List<Customer> findByOriginalUsername(@Param("email") String email);
}
