package com.kalsym.order.service.model;

import com.kalsym.order.service.enums.CartStage;
import com.kalsym.order.service.enums.ServiceType;
import com.kalsym.order.service.model.object.ItemDiscount;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
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
public class Cart implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String customerId;

    private String storeId;
    
    @Transient
    private String storeVoucherCode;
    
    @Transient
    private String deliveryQuotationId;
    
    @Transient
    private String deliveryType;
        
    @CreationTimestamp
    private Date created;
    @UpdateTimestamp
    private Date updated;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storeId", insertable = false, updatable = false)
    private StoreWithDetails store;
    
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

//    TODO
//    maybe add more fields here
    public void updateFromCOD(COD cod) {
        this.customerId = cod.getCustomerId();
        this.storeId = cod.getStoreId();
        this.isOpen = true;
        this.storeVoucherCode = cod.getStoreVoucherCode();
        ;
    }


    @Enumerated(EnumType.STRING)
    private CartStage stage;
    
    //service type
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;
}
