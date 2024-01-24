package com.kalsym.order.service.model;

import com.kalsym.order.service.enums.*;

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
import org.json.JSONObject;

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
 * @author Ayaan
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "voucher_serial_number")
@NoArgsConstructor
public class VoucherSerialNumber implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    private String voucherId;

    private Boolean isUsed;
    private String serialNumber;
    private String voucherRedeemCode;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expiryDate;

    @Enumerated(EnumType.STRING)
    private VoucherSerialStatus currentStatus;

    private String customer;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date redeemDate;

    private String storeDetails;

    private String qrDetails;

    public String storeDetails(String json, String value) {
        JSONObject jsonObject = new JSONObject(json);
        String data = null;

        if (value == "storeName") {
            data = jsonObject.getString("storeName");
        } else if (value == "storePhone") {
            data = jsonObject.getString("storePhone");
        }

        return data;
    }

    public String qrDetails(String json, String value) {
        JSONObject jsonObject = new JSONObject(json);
        String data = null;
        
        if (value == "phoneNumber") {
            data = jsonObject.getString("phoneNumber");
        } else if (value == "productName") {
            data = jsonObject.getString("productName");
        } else if (value == "productPrice") {
            data = jsonObject.getString("productPrice");
        } else if (value == "date") {
            data = jsonObject.getString("date");
        } else if (value == "storeId") {
            data = jsonObject.getString("storeId");
        } else if (value == "productImageUrl") {
            data = jsonObject.getString("productImageUrl");
        } else if (value == "voucherCode") {
            data = jsonObject.getString("voucherCode");
        } else if (value == "isGlobalStore") {
            data = jsonObject.getString("isGlobalStore");
        }

        return data;
    }
}
