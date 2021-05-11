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
public class DeliveryServiceDeliveryDetails {
   String deliveryAddress;   
   String deliveryPostcode;
   String deliveryState;
   String deliveryCity;
   String deliveryCountry;
   
   String deliveryContactName;
   String deliveryContactPhone;
   String deliveryContactEmail;
}
