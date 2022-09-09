package com.kalsym.order.service.model;

import com.kalsym.order.service.model.object.ItemDiscount;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author 7cu
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "product_inventory")
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductInventory implements Serializable {

    @Id
    private String itemCode;

    private Double price;
    private Double compareAtprice;

    private String SKU;
    
    @Transient
    private ItemDiscount itemDiscount;
    
    //private String name;
    private Integer quantity;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "productId", referencedColumnName = "id", insertable = false, updatable = false, nullable = true)
    private Product product;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemCode", referencedColumnName = "itemCode", insertable = false, updatable = false, nullable = true)    
    private List<ProductInventoryItem> productInventoryItems;
    
    private Double dineInPrice;
}
