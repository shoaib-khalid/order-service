package com.kalsym.order.service.model;

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
@Table(name = "order_completion_status")

public class OrderCompletionStatus {

    @Id
    private String status;
    private String description;

    public void update(OrderCompletionStatus orderCompletionStatus) {
        status = orderCompletionStatus.getStatus();
        description = orderCompletionStatus.getDescription();
    }
}
