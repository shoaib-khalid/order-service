package com.kalsym.order.service.model;

import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "cart")

/**
 * When a customer leaves an online store without making a purchase it is
 * recorded as an abandoned cart
 */
public class Cart {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String customerId;

    private String storeId;
    @CreationTimestamp
    private Date created;
    @UpdateTimestamp
    private Date updated;
   
    /*@OneToMany(fetch = FetchType.LAZY, mappedBy = "cartMain")
    private List<CartItem> cartItems;
    */
    
    /**
     * If the cart is still open, use to track when the cart is closed and order
     * is placed, in that case the cart is moved to orders table
     */
    private Boolean isOpen;

    public void update(Cart cart) {
        if (null != cart.getId()) {
            this.setId(cart.getId());
        }
        customerId = cart.getCustomerId();
        storeId = cart.getStoreId();
        isOpen = cart.getIsOpen();
    }
}
