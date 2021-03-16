package com.kalsym.order.service.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
    private String status;
    private Date created;
    private String modifiedBy;
    private String comments;

    public void update(OrderCompletionStatusUpdate orderCompletionStatusUpdate) {
        orderId = orderCompletionStatusUpdate.getOrderId();
        status = orderCompletionStatusUpdate.getStatus();
        created = orderCompletionStatusUpdate.getCreated();
        modifiedBy = orderCompletionStatusUpdate.getModifiedBy();
        comments = orderCompletionStatusUpdate.getComments();
    }

}
