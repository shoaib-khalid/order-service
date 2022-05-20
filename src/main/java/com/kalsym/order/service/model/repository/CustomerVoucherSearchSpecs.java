/*
 * Copyright (C) 2021 taufik
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.kalsym.order.service.model.repository;

import com.kalsym.order.service.enums.VoucherStatus;
import com.kalsym.order.service.enums.VoucherType;
import com.kalsym.order.service.model.Voucher;
import com.kalsym.order.service.model.CustomerVoucher;
import com.kalsym.order.service.model.VoucherVertical;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.JoinType;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
/**
 *
 * @author taufik
 */
public class CustomerVoucherSearchSpecs {
     /**
     * Accept two dates and example matcher
     *
     * @param currentDate     
     * @param voucherType
     * @param verticalCode
     * @param customerId
     * @param voucherStatus
     * @param voucherCode
     * @param isUsed
     * @param example
     * @return
     */
    public static Specification<CustomerVoucher> getSpecWithDatesBetween(
            Date currentDate, 
            VoucherType voucherType, String verticalCode, String customerId, VoucherStatus voucherStatus, 
            String voucherCode, Boolean isUsed,
            Example<CustomerVoucher> example) {

        return (Specification<CustomerVoucher>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            Join<CustomerVoucher, Voucher> voucher = root.join("voucher");
            ListJoin<Voucher, VoucherVertical> voucherVerticalList = voucher.joinList("voucherVerticalList", JoinType.LEFT);
            //Join<Voucher, VoucherVertical> voucherVertical = voucher.join("voucherVerticalList");
            
            if (currentDate != null) {
                
                //date1
                Predicate predicateForStartDate1 = builder.greaterThanOrEqualTo(voucher.get("endDate"), currentDate);
                Predicate predicateForEndDate1 = builder.lessThanOrEqualTo(voucher.get("startDate"), currentDate); 
                Predicate predicateForDate1 = builder.and(predicateForStartDate1, predicateForEndDate1);
                predicates.add(predicateForDate1);
                
                //NOTES : The SQL Server AND operator takes precedence over the SQL Server OR operator (just like a multiplication operation takes precedence over an addition operation).              
            }
            
            if (voucherType!=null) {
                predicates.add(builder.equal(voucher.get("voucherType"), voucherType));
            } 
          
            if (verticalCode!=null) {
                predicates.add(builder.equal(voucherVerticalList.get("verticalCode"), verticalCode));
            } 
            
            if (customerId!=null) {
                predicates.add(builder.equal(root.get("customerId"), customerId));
            } 
            
            if (voucherStatus!=null) {
                predicates.add(builder.equal(voucher.get("status"), voucherStatus));
            } 
            
            if (voucherCode!=null) {
                predicates.add(builder.equal(voucher.get("voucherCode"), voucherCode));
            } 
            
            if (isUsed!=null) {
                predicates.add(builder.equal(root.get("isUsed"), isUsed));
            } 
            
            Predicate predicateForTotalRedeem = builder.lessThanOrEqualTo(voucher.get("totalRedeem"), voucher.get("totalQuantity"));
            predicates.add(predicateForTotalRedeem);
            
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
