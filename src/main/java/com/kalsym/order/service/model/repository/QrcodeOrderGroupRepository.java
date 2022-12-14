/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.QrcodeOrderGroup;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author taufik
 */
@Repository
public interface QrcodeOrderGroupRepository extends PagingAndSortingRepository<QrcodeOrderGroup, String>, JpaRepository<QrcodeOrderGroup, String>, JpaSpecificationExecutor<QrcodeOrderGroup> {
                
        QrcodeOrderGroup findByQrToken(@Param("qrToken") String qrToken);
        
        QrcodeOrderGroup findByTableNoAndPaymentStatus(@Param("tableNo") String tableNo, @Param("paymentStatus") PaymentStatus paymentStatus);
}
