package com.kalsym.order.service.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
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
@Table(name = "order_completion_status_config")
@IdClass(OrderCompletionStatusConfigId.class)
public class OrderCompletionStatusConfig implements Serializable {

    @Id
    public Integer id;
    @Id
    public String verticalId;
    @Id
    public String status;
    @Id
    public Boolean storePickup;
    @Id
    public String storeDeliveryType;

    public Boolean emailToCustomer;
    public Boolean emailToStore;
    public Boolean rcMessage;
    public Boolean requestDelivery;
    public Boolean pushNotificationToMerchat;

    
    public String customerEmailContent;
    public String storeEmailContent;
    public String rcMessageContent;
    public String storePushNotificationContent;
    public String storePushNotificationTitle;
    public String comments;
    
    @CreationTimestamp
    Date created;

    @UpdateTimestamp
    Date updated;
}
