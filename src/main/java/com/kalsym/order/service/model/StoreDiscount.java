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
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 *
 * @author 7cu
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "store_discount")
@NoArgsConstructor
public class StoreDiscount implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    private String storeId;
    private String discountName;
    private String discountType;
    private Boolean isActive;
    private double maxDiscountAmount;
     
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startDate;
    
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endDate;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "storeDiscountId", insertable = false, updatable = false, nullable = true)    
    private List<StoreDiscountTier> storeDiscountTierList;

}
