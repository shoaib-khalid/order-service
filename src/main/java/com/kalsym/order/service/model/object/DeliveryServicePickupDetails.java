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
public class DeliveryServicePickupDetails {
    
    String parcelReadyTime;
    String pickupDate;
    String pickupTime;
    String pickupOption;
    String vehicleType;

    String pickupAddress;
    String pickupPostcode;
    String pickupState;
    String pickupCountry;
    String pickupCity;
    Integer pickupLocationId;

    String pickupContactName;
    String pickupContactPhone;
    String pickupContactEmail;
    boolean isTrolleyRequired;
    String remarks;
}
