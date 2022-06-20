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
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.CartWithDetails;
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
public class CartSearchSpecs {
     /**
     * Accept two dates and example matcher
     *
     * @param includeEmptyCart     
     * @param example
     * @return
     */
    public static Specification<CartWithDetails> getEmptyCart(
            Boolean includeEmptyCart,
            Example<CartWithDetails> example) {

        return (Specification<CartWithDetails>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            if (includeEmptyCart!=null && !includeEmptyCart) {
                ListJoin<CartWithDetails, CartItem> cartItemList = root.joinList("cartItems", JoinType.LEFT);            
                predicates.add(builder.isNotNull(cartItemList));
            }
                       
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
}
