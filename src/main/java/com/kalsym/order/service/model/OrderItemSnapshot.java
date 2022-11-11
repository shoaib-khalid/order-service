package com.kalsym.order.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import java.util.List;
import java.util.Date;
import javax.persistence.CascadeType;
import com.kalsym.order.service.enums.DiscountCalculationType;
import java.io.Serializable;
import javax.persistence.GenerationType;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "order_item_snapshot")
public class OrderItemSnapshot implements Serializable {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;

    private Date dt;
    private String itemCode;
    private String productId;
    private int totalOrder;
    

}
