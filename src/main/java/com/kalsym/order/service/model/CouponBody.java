package com.kalsym.order.service.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Ayaan Ahmad on 12/09/2023.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class CouponBody implements Serializable {
    private String cartId;
    private String paymentType;

    private List<String> cartItems;

    private String customerId;
    private String customerNotes;

    private OrderPaymentDetail orderPaymentDetails;
    private OrderShipmentDetail orderShipmentDetails;

    private String voucherCode;

    public CouponBody(String cartId, String paymentType,
                      List<String> cartItems, String customerId,
                      String customerNotes, OrderPaymentDetail orderPaymentDetails,
                      OrderShipmentDetail orderShipmentDetails, String voucherCode) {
        this.cartId = cartId;
        this.paymentType = paymentType;
        this.cartItems = cartItems;
        this.customerId = customerId;
        this.customerNotes = customerNotes;
        this.orderPaymentDetails = orderPaymentDetails;
        this.orderShipmentDetails = orderShipmentDetails;
        this.voucherCode = voucherCode;
    }



}

