package com.kalsym.order.service.model;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.kalsym.order.service.enums.DineInPack;

/**
 *
 * @author FaisalHayatJadoon
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class COD implements Serializable {
    
    private String cartId;
    private String customerId;
    private String customerNotes;
    //voucher code will be remove in next release
    private String voucherCode;
    private String storeVoucherCode;
    private OrderShipmentDetail orderShipmentDetails;
    private OrderPaymentDetail orderPaymentDetails;
    private List<CartItem> cartItems;
    private DineInPack dineInPack;
    
    public COD(String cartId, OrderShipmentDetail orderShipmentDetails, OrderPaymentDetail orderPaymentDetails) {
        this.cartId = cartId;
        this.orderShipmentDetails = orderShipmentDetails;
        this.orderPaymentDetails = orderPaymentDetails;
    }
    
}
