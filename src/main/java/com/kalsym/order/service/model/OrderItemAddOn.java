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
import javax.persistence.CascadeType;
import com.kalsym.order.service.enums.DiscountCalculationType;
import java.io.Serializable;
import javax.persistence.OneToOne;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "order_item_addon")
public class OrderItemAddOn implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String orderItemId;
    private String productAddOnId;
    private Float price;
    private Float productPrice;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "productAddOnId", referencedColumnName = "id", insertable = false, updatable = false, nullable = true)
    private ProductAddOn productAddOn;
    

}
