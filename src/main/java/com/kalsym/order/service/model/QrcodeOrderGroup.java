package com.kalsym.order.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kalsym.order.service.enums.CartStage;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.enums.ServiceType;
import com.kalsym.order.service.model.object.ItemDiscount;
import com.kalsym.order.service.utility.DateTimeUtil;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
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

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "qrcode_order_group")

/**
 * When a customer leaves an online store without making a purchase it is
 * recorded as an abandoned cart
 */
public class QrcodeOrderGroup implements Serializable {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;

    private String qrToken;
    private String invoiceNo;
    private String storeId;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private String tableNo;
    private String zone;
    
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date created;
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updated;
    
    private Double subTotal;
    private Double appliedDiscount;
    private Double deliveryCharges;
    private Double deliveryDiscount;
    private Double serviceCharges;       
    private Double totalAmount;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderQrGroupId", insertable = false, updatable = false, nullable = true)
    private List<OrderGroup> orderGroupList;
    
    public Double getTotalOrderAmount() {
        return convertToZero(subTotal) - convertToZero(appliedDiscount) + convertToZero(deliveryCharges) - convertToZero(deliveryDiscount) + convertToZero(serviceCharges);
    }
    
    private double convertToZero(Double d) {
        if (d==null)
            return 0;
        else
            return d;
    }
    
    @Transient
    private String orderTimeConverted;
    
    public String getOrderTimeConverted() {
        if (created!=null) {
            LocalDateTime datetime = DateTimeUtil.convertToLocalDateTimeViaInstant(created, ZoneId.of("Asia/Kuala_Lumpur"));
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return formatter1.format(datetime);                                                
        } else {
            return null;
        }
    }
    
    @Transient
    private List<OrderItemWithDetails> orderItemWithDetails;
    
}
