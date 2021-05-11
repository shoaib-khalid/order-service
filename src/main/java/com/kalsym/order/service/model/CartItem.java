package com.kalsym.order.service.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
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
@Table(name = "cart_item")
public class CartItem {

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
    private String SKU;
    
    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartId", insertable=false, updatable=false)
    private Cart cartMain;
    */
    
    public void update(CartItem cartitem) {
        quantity = cartitem.getQuantity();
        id = cartitem.getId();
        cartId = cartitem.getCartId();
        productId = cartitem.getProductId();
        itemCode = cartitem.getItemCode();
    }

}
