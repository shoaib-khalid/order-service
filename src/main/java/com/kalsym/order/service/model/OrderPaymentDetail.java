package com.kalsym.order.service.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
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
@Table(name = "order_payment_detail")
public class OrderPaymentDetail implements Serializable {

    private String accountName;
    private String gatewayId;
    //private String couponId;
    private Date time;
    @Id
    private String orderId;
    
    private String paymentChannel;
    
    private String  deliveryQuotationReferenceId;
    private Double deliveryQuotationAmount;
    
    @Column(columnDefinition = "TINYINT(1) default 0")
    private Boolean isCombinedDelivery;
    
    public void update(OrderPaymentDetail orderPaymentDetail) {

    }
}
