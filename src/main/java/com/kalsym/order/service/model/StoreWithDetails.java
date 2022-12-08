package com.kalsym.order.service.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.kalsym.order.service.enums.StorePaymentType;
import com.kalsym.order.service.enums.StoreAssetType;
import com.kalsym.order.service.enums.DineInOption;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@Table(name = "store")
@ToString
@NoArgsConstructor
public class StoreWithDetails implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String name;

    private String city;

    private String address;

    private String clientId;

    private String verticalCode;

    private String storeDescription;
    private String postcode;

    private String domain;

    private String liveChatOrdersGroupId;

    private String liveChatOrdersGroupName;

    private String liveChatCsrGroupId;

    private String liveChatCsrGroupName;

    private String regionCountryId;

    private String phoneNumber;

    private String regionCountryStateId;

    private Double serviceChargesPercentage;
    
//    @Enumerated(EnumType.STRING)
    private String paymentType;
    
    private String dineInPaymentType;
    
    @Enumerated(EnumType.STRING)
    private DineInOption dineInOption;
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date snoozeStartTime;
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date snoozeEndTime;
    
    private String snoozeReason;

    @OneToOne(cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    @JoinColumn(name = "regionCountryId", referencedColumnName = "id", insertable = false, updatable = false, nullable = true)
    private RegionCountry regionCountry;


    /*@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", insertable = false, updatable = false, nullable = true)
    private StoreAsset storeAsset;
    */
    
    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "storeId", insertable = false, updatable = false, nullable = true)
    private List<StoreAssets> storeAssets;
    
    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    @JoinColumn(name = "storeId", insertable = false, updatable = false, nullable = true)
    private List<StoreTiming> storeTiming;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", insertable = false, updatable = false, nullable = true)
    private StoreDeliveryDetail storeDeliveryDetail;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "verticalCode", insertable = false, updatable = false, nullable = true)
    private RegionVertical regionVertical;

    public void update(StoreWithDetails store) {

        if (null != store.getCity()) {
            city = store.getCity();
        }

        if (null != store.getName()) {
            name = store.getName();
        }

        if (null != store.getAddress()) {
            address = store.getAddress();
        }

        if (null != store.getClientId()) {
            clientId = store.getClientId();
        }
        if (null != store.getVerticalCode()) {
            verticalCode = store.getVerticalCode();
        }

        if (null != store.getStoreDescription()) {
            storeDescription = store.getStoreDescription();
        }

        if (null != store.getPostcode()) {
            postcode = store.getPostcode();
        }

        if (null != store.getRegionCountryId()) {
            regionCountryId = store.getRegionCountryId();
        }

        if (null != store.getPhoneNumber()) {
            phoneNumber = store.getPhoneNumber();
        }

        if (null != store.getRegionCountryStateId()) {
            regionCountryStateId = store.getRegionCountryStateId();
        }

        if (null != store.getServiceChargesPercentage()) {
            serviceChargesPercentage = store.getServiceChargesPercentage();
        }
        
        if (null != store.getPaymentType()) {
            paymentType = store.getPaymentType();
        }

    }
    
    
    public String getNameAbreviation() {
        String abbreviation = "";

        if (name.length() <= 3) {
            abbreviation = name;
        } else {
            String[] myName = name.split(" ");

            for (int i = 0; i < myName.length; i++) {
                String s = myName[i];
                abbreviation = abbreviation + s.charAt(0);

                if (abbreviation.length() == 3) {
                    break;
                }
            }
        }
        return abbreviation;
    }
    
    
    public String getStoreLogoUrl() {
        if (!storeAssets.isEmpty()) {
            for (int i=0;i<storeAssets.size();i++) {
                if (storeAssets.get(i).getAssetType()==StoreAssetType.LogoUrl) {
                    return storeAssets.get(i).getAssetUrl();
                }
            }
        }
        return null;
    }
    
    private Boolean dineInConsolidatedOrder;
}
