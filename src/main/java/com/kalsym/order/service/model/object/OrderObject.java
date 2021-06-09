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
    private Float subTotal;
    private Float total;
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
}
