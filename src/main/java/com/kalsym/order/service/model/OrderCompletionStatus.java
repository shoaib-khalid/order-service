package com.kalsym.order.service.model;

import com.kalsym.order.service.enums.OrderStatus;
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
@Table(name = "order_completion_status")

public class OrderCompletionStatus {

    @Id
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String description;

    public void update(OrderCompletionStatus orderCompletionStatus) {
        status = orderCompletionStatus.getStatus();
        description = orderCompletionStatus.getDescription();
    }
}
