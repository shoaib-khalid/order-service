package com.kalsym.order.service.model;

import com.kalsym.order.service.enums.OrderStatus;
import java.util.List;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Getter;

import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author FaisalHayatJadoon
 */
@Getter
@Setter
@ToString
public class Body {

    private String currency;
    private String deliveryAddress;
    private String deliveryCity;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    private Double deliveryCharges;
    private Float subTotal;
    private String invoiceId;
    private List<OrderItem> orderItems;
    private String storeAddress;
    private String storeName;
    
    
    public Body(){
        
    }
}
