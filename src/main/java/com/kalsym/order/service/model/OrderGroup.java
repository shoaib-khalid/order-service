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
import javax.persistence.Transient;
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
@Table(name = "order_group")
public class OrderGroup implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private Double subTotal;
    private Double deliveryCharges;
    private Double serviceCharges;
    private Double total;
    private String customerId;
    private Double appliedDiscount;
    private Double deliveryDiscount;    
            
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date created;
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updated;
      
    @Column(nullable = true)
    private Double platformVoucherDiscount;    
      
    private String platformVoucherId;

    private String paymentStatus;
    
    @Column(nullable = true)
    private Double paidAmount;
    
    @Column(nullable = true)
    private Double refundAmount; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "customerId", insertable = false, updatable = false)
    private Customer customer;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderGroupId", insertable = false, updatable = false, nullable = true)
    private List<OrderWithDetails> orderList;
    
    @Transient
    private String shipmentPhoneNumber;
    
    @Transient
    private String shipmentEmail;
    
    @Transient
    private String shipmentName;
    
    @Transient
    private String regionCountryId;
    
    public Double getTotalOrderAmount() {
        return subTotal + deliveryCharges + serviceCharges;
    }
   
}
