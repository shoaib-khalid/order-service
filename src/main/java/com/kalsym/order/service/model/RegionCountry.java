package com.kalsym.order.service.model;


import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author Sarosh
 */
@Getter
@Setter
@Entity
@Table(name = "region_country")
@ToString
@NoArgsConstructor
public class RegionCountry implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String name;
    private String region;
    private String currency;
    private String currencyCode;
    private String currencySymbol;
    private String timezone;
    private String countryCode;
    
    public void update(RegionCountry regionVertical) {
        if (null != regionVertical.getName()) {
            name = regionVertical.getName();
        }

    }

}
