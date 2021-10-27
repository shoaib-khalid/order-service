package com.kalsym.order.service.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    private OrderShipmentDetail orderShipmentDetails;
    private OrderPaymentDetail orderPaymentDetails;

    public COD(String cartId, OrderShipmentDetail orderShipmentDetails, OrderPaymentDetail orderPaymentDetails) {
        this.cartId = cartId;
        this.orderShipmentDetails = orderShipmentDetails;
        this.orderPaymentDetails = orderPaymentDetails;
    }
    
}
