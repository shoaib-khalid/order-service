package com.kalsym.order.service.model.repository;


import com.kalsym.order.service.model.Voucher;
import com.kalsym.order.service.model.VoucherSerialNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 *
 * @author ayaan
 */
public interface VoucherSerialNumberRepository extends PagingAndSortingRepository<Voucher, String>, JpaRepository<Voucher, String>, JpaSpecificationExecutor<Voucher> {

    @Query("SELECT m FROM VoucherSerialNumber m WHERE "
            + "m.voucherId = :voucherId "
            + "AND m.currentStatus='NEW' "
    )
    List<VoucherSerialNumber> findAvailableVoucherSerialNumbers(
            @Param("voucherId") String voucherId
    );

}
