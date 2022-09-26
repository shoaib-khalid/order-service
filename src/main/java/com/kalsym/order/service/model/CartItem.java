package com.kalsym.order.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import com.kalsym.order.service.enums.DiscountCalculationType;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author 7cu
 */
@Entity
@Setter
@Getter
@ToString
@Table(name = "cart_item")
public class CartItem implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private int quantity;

    private String cartId;
    private String productId;
    private String itemCode;
    private Float price;
    private Float productPrice;
    private Float weight;
    @JsonProperty("SKU")
    private String SKU;
    private String productName;
    private String specialInstruction;
    private String discountId;
    private Float normalPrice;
    private String discountLabel;
    private DiscountCalculationType discountCalculationType;
    private Float discountCalculationValue;
    @Temporal(TemporalType.TIMESTAMP)
    private Date discountCheckTimestamp;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartItemId", insertable = false, updatable = false, nullable = true)
    private List<CartSubItem> cartSubItem;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartItemId", insertable = false, updatable = false, nullable = true)
    private List<CartItemAddOn> cartItemAddOn;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemCode", referencedColumnName = "itemCode", insertable = false, updatable = false, nullable = true)
    private ProductInventory productInventory;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemCode", referencedColumnName = "itemCode", insertable = false, updatable = false, nullable = true)
    private ProductAsset productAsset;
  
    public void update(CartItem cartItem, float itemPrice) {
        quantity = cartItem.getQuantity();
        price = (Float.parseFloat(String.valueOf(quantity))) * itemPrice;
//        id = cartItem.getId();
//        cartId = cartItem.getCartId();
//        productId = cartItem.getProductId();
//        itemCode = cartItem.getItemCode();
//        productName = cartItem.getProductName();
//        SKU = cartItem.getSKU();
    }

    

}
