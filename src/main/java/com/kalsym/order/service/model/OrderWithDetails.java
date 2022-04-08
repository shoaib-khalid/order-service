package com.kalsym.order.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "order")
public class OrderWithDetails implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String storeId;
    private Double subTotal;
    private Double deliveryCharges;
    private Double total;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date created;
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updated;

    private String invoiceId;
    
    private Integer totalReminderSent;    
    
    @Column(nullable = true)
    private Double klCommission;

    @Column(nullable = true)
    private Double storeServiceCharges;

    @Column(nullable = true)
    private Double storeShare;

    private String paymentType;
    
    private String deliveryType;
    
    @Column(nullable = true)
    private Double appliedDiscount;
    
     @Column(nullable = true)
    private Double deliveryDiscount;
     
    private String appliedDiscountDescription ;
     
    private String deliveryDiscountDescription ;
    
    private Boolean beingProcess;
    
    private String discountId;
    
    private String discountCalculationType;
    
    private Double discountCalculationValue;
    
    private Double discountMaxAmount;
    
    private Double deliveryDiscountMaxAmount;
    
    private Boolean isRevised;
            
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
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storeId", insertable = false, updatable = false)
    private StoreWithDetails store;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "customerId", insertable = false, updatable = false)
    private Customer customer;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", insertable = false, updatable = false, nullable = true)
    private List<OrderRefund> orderRefund;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", insertable = false, updatable = false, nullable = true)
    private List<OrderItemWithDetails> orderItemWithDetails;
     
    public void update(Order order) {
        if (null != order.getStoreId()) {
            this.setStoreId(order.getStoreId());
        }
        subTotal = order.getSubTotal();
        storeServiceCharges = order.getStoreServiceCharges();
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
