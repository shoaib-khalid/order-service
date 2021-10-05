package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.Store;
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
public interface StoreRepository extends PagingAndSortingRepository<Store, String>, JpaRepository<Store, String> {
    
    @Query(value = "SELECT getInvoiceSeqNo(:storeId)", nativeQuery = true)
    int getInvoiceSeqNo(@Param("storeId") String storeId);
    
    @Modifying
    @Query("UPDATE Store m SET m.invoiceSeqNo=0 WHERE m.id = :storeId") 
    void ResetInvoiceSeqNo(
            @Param("storeId") String storeId
            );
}
