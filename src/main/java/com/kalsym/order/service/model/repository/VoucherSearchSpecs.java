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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.Predicate;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
/**
 *
 * @author taufik
 */
public class VoucherSearchSpecs {
     /**
     * Accept two dates and example matcher
     *
     * @param currentDate     
     * @param voucherType
     * @param storeId
     * @param verticalCode
     * @param example
     * @return
     */
    public static Specification<Voucher> getSpecWithDatesBetween(
            Date currentDate, 
            VoucherType voucherType, String storeId, String verticalCode,
            Example<Voucher> example) {

        return (Specification<Voucher>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            
            if (currentDate != null) {
                
                //date1
                Predicate predicateForStartDate1 = builder.greaterThanOrEqualTo(root.get("endDate"), currentDate);
                Predicate predicateForEndDate1 = builder.lessThanOrEqualTo(root.get("startDate"), currentDate); 
                Predicate predicateForDate1 = builder.and(predicateForStartDate1, predicateForEndDate1);
                predicates.add(predicateForDate1);
                
                //NOTES : The SQL Server AND operator takes precedence over the SQL Server OR operator (just like a multiplication operation takes precedence over an addition operation).              
            }
            
            if (voucherType!=null) {
                predicates.add(builder.equal(root.get("voucherType"), voucherType));
            } 
            
            if (storeId!=null) {
                predicates.add(builder.equal(root.get("storeId"), storeId));
            } 
            
            if (verticalCode!=null) {
                predicates.add(builder.equal(root.get("verticalCode"), verticalCode));
            } 
            
            Predicate predicateForTotalRedeem = builder.lessThanOrEqualTo(root.get("totalRedeem"), root.get("totalQuantity"));
            predicates.add(predicateForTotalRedeem);

            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
