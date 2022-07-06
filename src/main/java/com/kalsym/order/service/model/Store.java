package com.kalsym.order.service.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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
@Table(name = "store")
public class Store implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String name;
    private String city;
    private String address;
    private String clientId;
    private String postcode;
    private String state;
    private String contactName;
    private String phone;
    private String phoneNumber;
    private String email;
    private String verticalCode;
    private String regionCountryId;
    private String regionCountryStateId;
    private Double serviceChargesPercentage;

    private String paymentType;
    private Integer invoiceSeqNo;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "regionCountryId", insertable = false, updatable = false)
    private RegionCountry regionCountry;
    
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
}
