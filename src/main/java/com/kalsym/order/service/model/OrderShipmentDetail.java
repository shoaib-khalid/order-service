package com.kalsym.order.service.model;

import java.io.Serializable;
import javax.persistence.Entity;
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
    @Id
    private String orderId;
    
    public void update(OrderShipmentDetail orderShipmentDetail){
        
    }

}
