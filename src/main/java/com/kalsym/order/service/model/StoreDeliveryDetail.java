package com.kalsym.order.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kalsym.order.service.enums.DeliveryType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author 7cu
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class StoreDeliveryDetail implements Serializable {

    @Id
    private String storeId;
    
//    @Enumerated(EnumType.STRING)
    private String type;
    private String itemType;

  

    private Integer maxOrderQuantityForBike;

    public void update(StoreDeliveryDetail storeDeliveryDetail) {
        if (storeDeliveryDetail.getType() != null) {
            type = storeDeliveryDetail.getType();
        }

       
        if (storeDeliveryDetail.getItemType() != null) {
            itemType = storeDeliveryDetail.getItemType();
        }

        if (storeDeliveryDetail.getMaxOrderQuantityForBike() != null) {
            maxOrderQuantityForBike = storeDeliveryDetail.getMaxOrderQuantityForBike();
        }

    }

}
