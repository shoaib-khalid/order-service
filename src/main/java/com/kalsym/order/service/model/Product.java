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
import javax.persistence.Transient;

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
    private String seoName;    
    private boolean trackQuantity;
    private boolean allowOutOfStockPurchases;
    private int minQuantityForAlarm;
    private Boolean isPackage;
    private int shortId;
    
    public enum Status {
        ACTIVE,
        DELETED,
        INACTIVE,
        DRAFT,
        OUTOFSTOCK;
    }
    
    @Column(columnDefinition = "ENUM('ACTIVE', 'DELETED', 'DRAFT', 'INACTIVE', 'OUTOFSTOCK')")
    @Enumerated(EnumType.STRING)
    private Status status;
    
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;
    
    public String getThumbnailUrl() {
        if (thumbnailUrl==null)
            return null;
        else
            return OrderServiceApplication.ASSETURL + "/" + thumbnailUrl;
    }
    
    @Transient
    String seoNameMarketplace;
    public String getSeoNameMarketplace() {
        return shortId+"-"+seoName;
    }
   
}
