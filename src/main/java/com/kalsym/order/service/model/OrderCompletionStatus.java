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

/**
 * When a customer leaves an online store without making a purchase it is recorded as an abandoned cart
 */
public class OrderCompletionStatus {

    @Id
    private String status;
    
    private String description;
   
}
