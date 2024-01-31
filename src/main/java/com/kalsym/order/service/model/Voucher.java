package com.kalsym.order.service.model;

import com.kalsym.order.service.enums.VoucherDiscountType;
import com.kalsym.order.service.enums.DiscountCalculationType;
import com.kalsym.order.service.enums.VoucherStatus;
import com.kalsym.order.service.enums.VoucherType;
import java.io.Serializable;
import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import java.util.Date;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

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
    private Double discountValue;
    private Double maxDiscountAmount;
    private String voucherCode;
    private Integer totalQuantity;
    private Integer totalRedeem;
    private String currencyLabel;
    private Boolean isNewUserVoucher;
    private Boolean checkTotalRedeem;
    private Double minimumSpend;
    private Boolean allowDoubleDiscount;
    private Boolean requireToClaim;
    private Boolean allowMultipleRedeem;
    private String storeId;
    
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

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date created_at;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updated_at;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucherId", insertable = false, updatable = false, nullable = true)
    private List<VoucherTerms> voucherTerms;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucherId", insertable = false, updatable = false, nullable = true)
    private List<VoucherVertical> voucherVerticalList;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucherId", insertable = false, updatable = false, nullable = true)
    private List<VoucherStore> voucherStoreList;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucherId", insertable = false, updatable = false, nullable = true)
    private List<VoucherServiceType> voucherServiceTypeList;

    private Boolean isGlobalStore;
    
    public boolean getAllowMultipleRedeem() {
        if (allowMultipleRedeem==null)
            return false;
        else 
            return allowMultipleRedeem;
    }

    public void update(Voucher voucher) {
        if (null != voucher.getName()) {
            name = voucher.getName();
        }

        if (null != voucher.getDiscountValue()) {
            discountValue = voucher.getDiscountValue();
        }

        if (null != voucher.getMaxDiscountAmount()) {
            maxDiscountAmount = voucher.getMaxDiscountAmount();
        }

        if (null != voucher.getMaxDiscountAmount()) {
            maxDiscountAmount = voucher.getMaxDiscountAmount();
        }

        if (null != voucher.getVoucherCode()) {
            voucherCode = voucher.getVoucherCode();
        }

        if (null != voucher.getTotalQuantity()) {
            totalQuantity = voucher.getTotalQuantity();
        }

//        if (null != voucher.getTotalRedeem()) {
//            totalRedeem = voucher.getTotalRedeem();
//        }

        if (null != voucher.getCurrencyLabel()) {
            currencyLabel = voucher.getCurrencyLabel();
        }

        if (null != voucher.getIsNewUserVoucher()) {
            isNewUserVoucher = voucher.getIsNewUserVoucher();
        }

        if (null != voucher.getCheckTotalRedeem()) {
            checkTotalRedeem = voucher.getCheckTotalRedeem();
        }

        if (null != voucher.getMinimumSpend()) {
            minimumSpend = voucher.getMinimumSpend();
        }

        if (null != voucher.getAllowDoubleDiscount()) {
            allowDoubleDiscount = voucher.getAllowDoubleDiscount();
        }

        if (null != voucher.getRequireToClaim()) {
            requireToClaim = voucher.getRequireToClaim();
        }

        allowMultipleRedeem = voucher.getAllowMultipleRedeem();

        if (null != voucher.getStatus()) {
            status = voucher.getStatus();
        }

        if (null != voucher.getVoucherType()) {
            voucherType = voucher.getVoucherType();
        }

        if (null != voucher.getDiscountType()) {
            discountType = voucher.getDiscountType();
        }

        if (null != voucher.getCalculationType()) {
            calculationType = voucher.getCalculationType();
        }

        if (null != voucher.getStartDate()) {
            startDate = voucher.getStartDate();
        }

        if (null != voucher.getEndDate()) {
            endDate = voucher.getEndDate();
        }

    }

    public String getFormattedEndDate() {
        // Convert java.util.Date to java.time.LocalDateTime
        LocalDateTime localDateTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // Define the output date format
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

        // Format the date as per the desired format
        return localDateTime.format(outputFormatter);
    }

}
