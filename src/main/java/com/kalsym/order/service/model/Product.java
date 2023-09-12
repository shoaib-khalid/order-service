package com.kalsym.order.service.model;

import javax.persistence.*;

import com.kalsym.order.service.enums.ProductType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import com.kalsym.order.service.enums.VehicleType;
import com.kalsym.order.service.OrderServiceApplication;

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
    private String packingSize;
    private Boolean isCustomPrice;
            
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

    private String voucherId;

    @OneToOne()
    @JoinColumn(name = "voucherId", referencedColumnName="id", insertable = false, updatable = false)
    private Voucher voucher;

    @Enumerated(EnumType.STRING)
    private ProductType productType;
}
