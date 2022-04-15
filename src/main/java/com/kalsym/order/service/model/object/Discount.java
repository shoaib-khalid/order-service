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

/**
 *
 * @author taufik
 */

@Getter
@Setter
@ToString
public class Discount {
    
    BigDecimal subTotalDiscount;
    BigDecimal deliveryDiscount;
    String subTotalDiscountDescription;
    String deliveryDiscountDescription;
    BigDecimal cartSubTotal; 
    String discountType;
    String discountCalculationType;
    BigDecimal discountCalculationValue;
    String discountId;
    BigDecimal discountMaxAmount;
    BigDecimal deliveryDiscountMaxAmount;
    
    BigDecimal storeServiceCharge;
    BigDecimal storeServiceChargePercentage;
    BigDecimal cartDeliveryCharge;
    BigDecimal cartGrandTotal;
    
    Double voucherSubTotalDiscount;
    Double voucherDeliveryDiscount;
    String voucherSubTotalDiscountDescription;
    String voucherDeliveryDiscountDescription;
    String voucherDiscountType;
    String voucherDiscountCalculationType;
    Double voucherDiscountCalculationValue;
    Double voucherDiscountMaxAmount;
}
