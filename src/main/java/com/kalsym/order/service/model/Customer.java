package com.kalsym.order.service.model;

import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
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
@Table(name = "customer")

/**
 * When a customer leaves an online store without making a purchase it is
 * recorded as an abandoned cart
 */
public class Customer {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String name;

    private String phoneNumber;
    private String email;
    private String storeId;
    
    @CreationTimestamp
    private Date created;
    @UpdateTimestamp
    private Date updated;
   
    private Boolean isActivated;
    
    private String countryId;
    
    private String originalEmail;
    
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "countryId", insertable = false, updatable = false)
    private RegionCountry regionCountry;
}
