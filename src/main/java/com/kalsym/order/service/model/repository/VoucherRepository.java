package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.Voucher;
import java.util.List;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author 7cu
 */

@Repository
public interface VoucherRepository extends PagingAndSortingRepository<Voucher, String>, JpaRepository<Voucher, String>, JpaSpecificationExecutor<Voucher> {
    
    @Query("SELECT m FROM Voucher m WHERE "
            + "m.voucherCode = :voucherCode "
            + "AND m.status='ACTIVE' "
            + "AND m.startDate < :currentDate AND m.endDate > :currentDate "
            + "AND m.totalRedeem < m.totalQuantity") 
    Voucher findAvailableVoucherByCode(
            @Param("voucherCode") String voucherCode,
            @Param("currentDate") Date currentDate
           );
}
