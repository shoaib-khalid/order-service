package com.kalsym.order.service.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
//@Table(name = "`order`")
@Table(name = "order")
public class Order {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String storeId;
    private Float subTotal;
    private Float total;
    private String completionStatus;
    private String paymentStatus;
    private String customerNotes;
    private String privateAdminNotes;
    private String cartId;
    private String customerId;
    @CreationTimestamp
    private Date created;
    @UpdateTimestamp
    private Date updated;

    public void update(Order order) {
        if (null != order.getStoreId()) {
            this.setStoreId(order.getStoreId());
        }
        subTotal = order.getSubTotal();
        total = order.getTotal();
        completionStatus = order.getCompletionStatus();
        paymentStatus = order.getPaymentStatus();
        customerNotes = order.getCustomerNotes();
        privateAdminNotes = order.getPrivateAdminNotes();
        customerId = order.getCustomerId();
        cartId = order.getCartId();
        //created = order.getCreated();
        //updated = order.getUpdated();
    }
}
