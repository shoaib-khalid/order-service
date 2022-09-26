package com.kalsym.order.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
@Setter
@Getter
@ToString
@Table(name = "cart_item_addon")
public class CartItemAddOn implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String cartItemId;
    private String productAddOnId;
    private Float price;
   
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "productAddOnId", referencedColumnName = "id", insertable = false, updatable = false, nullable = true)
    private ProductAddOn productAddOn;
        
}
