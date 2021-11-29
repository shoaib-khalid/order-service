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

/**
 *
 * @author 7cu
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String name;
//    private String stock;
    private String storeId;
    private String categoryId;
    private String thumbnailUrl;
    private String vendor;
    private String description;
//    private String barcode;
    private String region;


//    private Float weight;
//    private String deliveryType;
//    private String itemType;
    private String seoUrl;
    private boolean trackQuantity;
    private boolean allowOutOfStockPurchases;
    private int minQuantityForAlarm;
    private Boolean isPackage;
    
    public enum Status {
        ACTIVE,
        DELETED,
        INACTIVE,
        DRAFT;
    }
    
    @Column(columnDefinition = "ENUM('ACTIVE', 'DELETED', 'DRAFT', 'INACTIVE')")
    @Enumerated(EnumType.STRING)
    private Status status;
   
}
