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
 * @author user
 */

@Getter
@Setter
@ToString
public class DeliveryServiceSubmitOrder {
    private String orderId;
    private String storeId;
    private String customerId;
    private String productCode;
    private String itemType;
    private Integer deliveryProviderId;
    private Integer totalWeightKg;
    private String shipmentContent;
    private Integer pieces;
    private Boolean IsInsurance;
    private Float shipmentValue;
    private DeliveryServiceDeliveryDetails delivery;
    private DeliveryServicePickupDetails  pickup;
    
}
