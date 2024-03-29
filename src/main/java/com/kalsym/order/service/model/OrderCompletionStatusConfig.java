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
    @Id
    public String paymentType;

    public int statusSequence;
    public Boolean emailToCustomer;
    public Boolean emailToStore;
    public Boolean emailToFinance;
    public Boolean rcMessage;
    public Boolean requestDelivery;
    public Boolean pushNotificationToMerchat;
    public Boolean pushWAToAdmin;
    
    public String customerEmailContent;
    public String storeEmailContent;
    public String financeEmailContent;
    public String rcMessageContent;
    public String storePushNotificationContent;
    public String storePushNotificationTitle;
    public String comments;
    public String nextActionText;
    
    public Boolean pushWAToCustomer;
    public String pushWAToCustomerTemplateName;
    public String pushWAToCustomerTemplateFormat;    
    
    @CreationTimestamp
    Date created;

    @UpdateTimestamp
    Date updated;
}
