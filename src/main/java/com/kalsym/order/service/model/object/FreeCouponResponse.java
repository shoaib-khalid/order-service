package com.kalsym.order.service.model.object;

import java.util.Date;

import com.kalsym.order.service.enums.VoucherSerialStatus;
import com.kalsym.order.service.enums.VoucherStatus;
import com.kalsym.order.service.model.Voucher;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FreeCouponResponse {
    
    String phoneNumber;
    String invoiceId;
    String name;
    Double price;
    Date startDate;
    Date endDate;
    String storeName;
    String storeId;
    VoucherStatus status;
    String voucherCode;
    String voucherImage;
    VoucherSerialStatus redeemStatus;
    String voucherType;
    Boolean isGlobalStore;

    String orderId;
    String orderItemId;

    Voucher voucher;
  
}
