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

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "order_subitem")
public class OrderSubItem {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String orderItemId;
    private String productId;
    private Float price;
    private Float productPrice;
    private Float weight;
    @JsonProperty("SKU")
    private String SKU;
    private int quantity;
    private String itemCode;
    private String productName;
    private String specialInstruction;
    private String productVariant;
    
    
   
    public void update(OrderItem orderitem) {
        id = orderitem.getId();
        orderItemId = orderitem.getOrderId();
        productId = orderitem.getProductId();
        price = orderitem.getPrice();
        productPrice = orderitem.getProductPrice();
        weight = orderitem.getWeight();
        SKU = orderitem.getSKU();
        quantity = orderitem.getQuantity();
        itemCode = orderitem.getItemCode();
    }

}
