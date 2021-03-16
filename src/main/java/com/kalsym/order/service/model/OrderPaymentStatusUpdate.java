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
@Table(name = "order_payment_status_update")

public class OrderPaymentStatusUpdate {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String status;

    private Date created;

    private String modifiedBy;
    private String comments;
    private String orderId;

    public void update(OrderPaymentStatusUpdate orderPaymentStatusUpdate) {
        orderId = orderPaymentStatusUpdate.getOrderId();
        status = orderPaymentStatusUpdate.getStatus();
        created = orderPaymentStatusUpdate.getCreated();
        modifiedBy = orderPaymentStatusUpdate.getModifiedBy();
        comments = orderPaymentStatusUpdate.getComments();
    }

}
