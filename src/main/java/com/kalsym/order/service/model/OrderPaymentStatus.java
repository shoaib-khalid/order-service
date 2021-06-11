package com.kalsym.order.service.model;

import com.kalsym.order.service.enums.PaymentStatus;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "order_payment_status")

public class OrderPaymentStatus {

    @Id
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String description;

    public void update(OrderPaymentStatus orderPaymentStatus) {
        status = orderPaymentStatus.getStatus();
        description = orderPaymentStatus.getDescription();
    }
}
