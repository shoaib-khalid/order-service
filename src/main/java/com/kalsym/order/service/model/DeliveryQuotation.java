
package com.kalsym.order.service.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import com.kalsym.order.service.enums.VehicleType;

/**
 *
 * @author Faisal Hayat
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DeliveryQuotation {
    
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;
    String storeId;
    String orderId;
    String cartId;
    Double amount; 
    VehicleType vehicleType;
    String fulfillmentType;
    Boolean combinedDelivery;
}
