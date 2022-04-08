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
@Table(name = "order_item")
public class OrderItemWithDetails implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String orderId;
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
    private String discountId;
    private Float normalPrice;
    private String discountLabel;
    private String status;
    private Integer originalQuantity;
    private DiscountCalculationType discountCalculationType;
    private Float discountCalculationValue;
    
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "orderItemId", insertable = false, updatable = false, nullable = true)
    private List<OrderSubItem> orderSubItem;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemCode", referencedColumnName="itemCode", insertable = false, updatable = false, nullable = true)
    private ProductAsset itemAssetDetails;    
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemCode", referencedColumnName = "itemCode", insertable = false, updatable = false, nullable = true)
    private ProductInventory productInventory;
    
    /*@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product productDetails;
    */
    
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", insertable=false, updatable=false)
    private Order orderMain;
    */
   
    public void update(OrderItem orderitem) {
        id = orderitem.getId();
        orderId = orderitem.getOrderId();
        productId = orderitem.getProductId();
        price = orderitem.getPrice();
        productPrice = orderitem.getProductPrice();
        weight = orderitem.getWeight();
        SKU = orderitem.getSKU();
        quantity = orderitem.getQuantity();
        itemCode = orderitem.getItemCode();
    }

}
