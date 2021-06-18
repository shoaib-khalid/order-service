package com.kalsym.order.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Setter
@Getter
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
    @JsonProperty("SKU")
    private String SKU;
    private String productName;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartId", insertable=false, updatable=false)
    private Cart cartMain;
     */
    public void update(CartItem cartItem) {
        quantity = cartItem.getQuantity();
        price = (Float.parseFloat(String.valueOf(quantity))) * this.productPrice;
//        id = cartItem.getId();
//        cartId = cartItem.getCartId();
//        productId = cartItem.getProductId();
//        itemCode = cartItem.getItemCode();
//        productName = cartItem.getProductName();
//        SKU = cartItem.getSKU();
    }

    

}
