package com.kalsym.order.service.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import com.kalsym.order.service.enums.VehicleType;
import com.kalsym.order.service.OrderServiceApplication;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "product_addon")
public class ProductAddOn {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String productId;
    private String addonTemplateItemId;
    private Double price;
    private Double dineInPrice;
    private Integer sequenceNumber;
    private String productAddonGroupId;
    
    public enum Status {
        AVAILABLE,
        NOTAVAILABLE,
        OUTOFSTOCK,
        DELETED;
    }
    
    @Column(columnDefinition = "ENUM('ACTIVE', 'DELETED', 'DRAFT', 'INACTIVE', 'OUTOFSTOCK')")
    @Enumerated(EnumType.STRING)
    private Status status;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "addonTemplateItemId", referencedColumnName = "id", insertable = false, updatable = false, nullable = true)    
    private AddOnTemplateItem addOnTemplateItem;
      
}
