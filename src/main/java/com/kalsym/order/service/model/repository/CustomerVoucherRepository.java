package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.CustomerVoucher;
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
public interface CustomerVoucherRepository extends PagingAndSortingRepository<CustomerVoucher, String>, JpaRepository<CustomerVoucher, String>, JpaSpecificationExecutor<CustomerVoucher>, CustomRepository<CustomerVoucher, String> {
    
    @Query("SELECT m FROM CustomerVoucher m "
            + "WHERE m.customerId = :queryCustomerId AND m.isUsed=0 "
            + "AND m.voucher.status='ACTIVE' "
            + "AND m.voucher.startDate < :currentDate AND m.voucher.endDate > :currentDate "
            + "AND m.voucher.voucherCode = :queryVoucherCode "
            + "AND ("
                + "(m.voucher.totalRedeem < m.voucher.totalQuantity AND m.checkTotalRedeem=true) OR "
                + "(m.checkTotalRedeem=false) "
            + ")")           
    CustomerVoucher findCustomerVoucherByCode(
            @Param("queryCustomerId") String queryCustomerId,
            @Param("queryVoucherCode") String queryVoucherCode,
            @Param("currentDate") Date currentDate
            );
    
    
    CustomerVoucher findByCustomerIdAndVoucherId(@Param("customerId") String customerId, @Param("voucherId") String voucherId);
    
}
