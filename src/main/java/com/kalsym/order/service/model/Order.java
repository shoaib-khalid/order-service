package com.kalsym.order.service.model;

import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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
public class Order implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String storeId;
    private Float subTotal;
    private Float serviceCharges;
    private Double deliveryCharges;
    private Float total;
    @Enumerated(EnumType.STRING)
    private OrderStatus completionStatus;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private String customerNotes;
    private String privateAdminNotes;
    private String cartId;
    private String customerId;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    private String invoiceId;

    /*
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="orderId", insertable = false, updatable = false)
    private OrderPaymentDetail orderPaymentDetail;
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private OrderShipmentDetail orderShipmentDetail;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private OrderPaymentDetail orderPaymentDetail;

    /*    @OneToMany(fetch = FetchType.LAZY, mappedBy = "orderMain")
    private List<OrderItem> orderItem;
     */
    public void update(Order order) {
        if (null != order.getStoreId()) {
            this.setStoreId(order.getStoreId());
        }
        subTotal = order.getSubTotal();
        serviceCharges = order.getServiceCharges();
        deliveryCharges = order.getDeliveryCharges();
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
