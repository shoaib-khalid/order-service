/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.object;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author 7cu
 */
@Getter
@Setter
@ToString
public class OrderObject {

    private String storeId;
    private Double subTotal;
    private Double total;
    private String completionStatus;
    private String paymentStatus;
    private String customerNotes;
    private String privateAdminNotes;
    private String cartId;
    private String customerId;
    private String deliveryContactName;
    private String deliveryAddress;
    private String deliveryContactPhone;
    private String deliveryPostcode;
    private String deliveryCity;
    private String deliveryState;
    private String deliveryCountry;
    private String deliveryEmail;
    private Integer deliveryProviderId;

    private String accountName;
    private String gatewayId;
    private String couponId;
    private Date time;
    private String orderId;
    private String deliveryQuotationReferenceId;
    private Double deliveryQuotationAmount;
    
    private Double appliedDiscount;
    private String appliedDiscountDescription;
    private Double deliveryDiscount;
    private String deliveryDiscountDescription;
    private Double storeServiceCharge;
    private Double klCommission;
    private Double grandTotal;
    private Double storeShare;
    
    private String discountCalculationType;
    private Double discountCalculationValue;
    private String discountType;
    private String discountId;
    private Double discountMaxAmount;
    private Double deliveryDiscountMaxAmount;
    
    private Double voucherSubTotalDiscount;
    private String voucherSubTotalDiscountDescription;
    private Double voucherDeliveryDiscount;
    private String voucherDeliveryDiscountDescription;
    private String voucherDiscountCalculationType;
    private Double voucherDiscountCalculationValue;
    private String voucherDiscountType;
    private Double voucherDiscountMaxAmount;
    private Double voucherDeliveryDiscountMaxAmount;
    private Double voucherDiscount;
    private String voucherId;
    
    private Double storeVoucherSubTotalDiscount;
    private String storeVoucherSubTotalDiscountDescription;
    private Double storeVoucherDeliveryDiscount;
    private String storeVoucherDeliveryDiscountDescription;
    private String storeVoucherDiscountCalculationType;
    private Double storeVoucherDiscountCalculationValue;
    private String storeVoucherDiscountType;
    private Double storeVoucherDiscountMaxAmount;
    private Double storeVoucherDeliveryDiscountMaxAmount;
    private Double storeVoucherDiscount;
    private String storeVoucherId;
    
    private Boolean gotError;
    private String errorMessage;
}
