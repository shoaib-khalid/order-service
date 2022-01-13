package com.kalsym.order.service.model;

import com.kalsym.order.service.enums.DeliveryType;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.persistence.Id;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "order_shipment_detail")

/**
 *
 * @author 7cu
 */
public class OrderShipmentDetail implements Serializable {

    private String receiverName;
    private String phoneNumber;
    private String address;
    private String city;
    private String zipcode;
    private String email;
    private Integer deliveryProviderId;
    private String state;
    private String country;
    private String trackingUrl;
    @Id
    private String orderId;

    private Boolean storePickup;

    private String merchantTrackingUrl;
    private String customerTrackingUrl;

    private String trackingNumber;
    
    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;
    
    public void update(OrderShipmentDetail orderShipmentDetail) {
        if (orderShipmentDetail.getTrackingNumber() != null) {
            trackingNumber = orderShipmentDetail.getTrackingNumber();
        }

        if (orderShipmentDetail.getCustomerTrackingUrl() != null) {
            customerTrackingUrl = orderShipmentDetail.getCustomerTrackingUrl();
        }
        
        if (orderShipmentDetail.getCustomerTrackingUrl() != null) {
            merchantTrackingUrl = orderShipmentDetail.getMerchantTrackingUrl();
        }
    }

}
