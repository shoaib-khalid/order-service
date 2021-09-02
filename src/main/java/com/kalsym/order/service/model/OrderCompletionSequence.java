package com.kalsym.order.service.model;

import com.kalsym.order.service.enums.OrderStatus;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "order_completion_status_sequence")
@IdClass(OrderCompletionSequenceId.class)
public class OrderCompletionSequence implements Serializable {

    @Id
    public Integer id;
    @Id
    public String verticalId;
    @Id
    public String status;
    @Id
    public String storePickup;
    @Id
    public String storeDeliveryType;

    public String emailToCustomer;
    public String emailToStore;
    public String rcMessage;
    public String customerEmailContent;
    public String storeEmailContent;
    public String rcMessageContect;
    public String comments;
    
    @CreationTimestamp
    Date created;

    @UpdateTimestamp
    Date updated;
}
