package com.kalsym.order.service.model;

import com.kalsym.order.service.enums.OrderStatus;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
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
@NoArgsConstructor
@ToString
public class OrderCompletionSequenceId implements Serializable {

    public Integer id;
    public String verticalId;
    public String status;
    public String storePickup;
    public String storeDeliveryType;

}
