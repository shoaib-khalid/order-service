package com.kalsym.order.service.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author FaisalHayatJadoon
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "store_asset")
public class StoreAsset implements Serializable{
    @Id
    private String storeId;
    private String logoUrl;
    private String bannerUrl;

    public StoreAsset(String storeId, String logoUrl, String bannerUrl) {
        this.storeId = storeId;
        this.logoUrl = logoUrl;
        this.bannerUrl = bannerUrl;
    }
    
}