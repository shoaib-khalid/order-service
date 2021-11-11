package com.kalsym.order.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kalsym.order.service.enums.OrderStatus;
import java.util.Date;
import java.sql.Time;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "order_completion_status_update")

/**
 * When a customer leaves an online store without making a purchase it is
 * recorded as an abandoned cart
 */
public class OrderCompletionStatusUpdate {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String orderId;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @CreationTimestamp
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date created;
    private String modifiedBy;
    private String comments;
    private String pickupDate;
    private String pickupTime;
    
    public void update(OrderCompletionStatusUpdate orderCompletionStatusUpdate) {
        orderId = orderCompletionStatusUpdate.getOrderId();
        status = orderCompletionStatusUpdate.getStatus();
        created = orderCompletionStatusUpdate.getCreated();
        modifiedBy = orderCompletionStatusUpdate.getModifiedBy();
        comments = orderCompletionStatusUpdate.getComments();
    }

}
