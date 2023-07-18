package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.model.VoucherStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface VoucherStoreRepository extends PagingAndSortingRepository<VoucherStore, String>, JpaRepository<VoucherStore, String>, JpaSpecificationExecutor<VoucherStore> {
    @Transactional
    @Modifying
    @Query("DELETE FROM VoucherStore vt WHERE vt.voucherId = :voucherId")
    void deleteByVoucherId(@Param("voucherId") String voucherId);
}
