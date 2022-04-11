package com.kalsym.order.service.model;

import com.kalsym.order.service.enums.VoucherDiscountType;
import com.kalsym.order.service.enums.DiscountCalculationType;
import com.kalsym.order.service.enums.VoucherStatus;
import com.kalsym.order.service.enums.VoucherType;
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
import java.time.LocalTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author 7cu
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "voucher")
@NoArgsConstructor
public class Voucher implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    private String name;    
    private String storeId;    
    private Double discountValue;
    private Double maxDiscountAmount;
    private String voucherCode;
    private Integer totalQuantity;
    private Integer totalRedeem;
    
    @Enumerated(EnumType.STRING)
    private VoucherStatus status;
    
    @Enumerated(EnumType.STRING)
    private VoucherType voucherType;
    
    @Enumerated(EnumType.STRING)
    private VoucherDiscountType discountType;
    
    @Enumerated(EnumType.STRING)
    private DiscountCalculationType calculationType;    
    
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startDate;
   
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endDate;
   

}
