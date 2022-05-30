/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.object;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 *
 * @author taufik
 */

@Getter
@Setter
@ToString
public class GroupDiscount {
    
    BigDecimal sumSubTotalDiscount;
    BigDecimal sumDeliveryDiscount;
    BigDecimal sumCartSubTotal;     
    BigDecimal sumCartDeliveryCharge;
    BigDecimal sumCartGrandTotal;
   
    List<Discount> storeDiscountList;
    
    Double platformVoucherSubTotalDiscount;
    Double platformVoucherDeliveryDiscount;
    String platformVoucherSubTotalDiscountDescription;
    String platformVoucherDeliveryDiscountDescription;
    String platformVoucherDiscountType;
    String platformVoucherDiscountCalculationType;
    Double platformVoucherDiscountCalculationValue;
    Double platformVoucherDiscountMaxAmount;
    
}
