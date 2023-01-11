package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.PaymentChannel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author 7cu
 */
@Repository
public interface PaymentChannelRepository extends PagingAndSortingRepository<PaymentChannel, String>, JpaRepository<PaymentChannel, String> {
        
}
