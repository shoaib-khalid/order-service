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
public class DiscountVoucher {
    
    BigDecimal subTotalDiscount;
    BigDecimal deliveryDiscount;
    String discountType;
    String discountCalculationType;
    BigDecimal discountCalculationValue;
    String voucherId;
    BigDecimal discountMaxAmount;
    BigDecimal deliveryDiscountMaxAmount;
        
}
