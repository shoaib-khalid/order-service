/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.object;

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
    
    Double subTotalDiscount;
    Double deliveryDiscount;
    String subTotalDiscountDescription;
    String deliveryDiscountDescription;
    Double cartSubTotal; 
    String discountType;
    String discountCalculationType;
    Double discountCalculationValue;
    String discountId;
    Double discountMaxAmount;
    Double deliveryDiscountMaxAmount;
    
    Double storeServiceCharge;
    Double storeServiceChargePercentage;
    Double cartDeliveryCharge;
    Double cartGrandTotal;
}
