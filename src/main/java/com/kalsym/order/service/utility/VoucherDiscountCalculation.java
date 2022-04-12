/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.utility;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.VoucherDiscountType;
import com.kalsym.order.service.enums.VoucherType;
import com.kalsym.order.service.model.Voucher;
import com.kalsym.order.service.enums.DiscountCalculationType;
import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.StoreDiscount;
import com.kalsym.order.service.model.StoreDiscountTier;
import com.kalsym.order.service.model.CustomerVoucher;
import com.kalsym.order.service.model.object.DiscountVoucher;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.StoreDiscountRepository;
import com.kalsym.order.service.model.repository.StoreDiscountTierRepository;
import com.kalsym.order.service.utility.Utilities;

import java.util.Optional;
import java.util.Date;
import java.util.List;

/**
 *
 * @author taufik
 */
public class VoucherDiscountCalculation {
    
    public static DiscountVoucher CalculateVoucherDiscount(Cart cart, double deliveryCharge, double subTotalAmount, 
            CustomerVoucher customerVoucher, String logprefix) {
        
        DiscountVoucher discount = new DiscountVoucher(); 
        Voucher voucher = customerVoucher.getVoucher();
        double subTotalDiscount=0.00;
        if (voucher.getDiscountType()==VoucherDiscountType.SHIPPING) {
            subTotalDiscount = CalculateDiscount(VoucherDiscountType.SHIPPING, voucher.getCalculationType(), voucher.getDiscountValue(), subTotalAmount, deliveryCharge, voucher.getMaxDiscountAmount());                        
            discount.setDeliveryDiscount(Utilities.roundDouble(subTotalDiscount,2));
        } else if (voucher.getDiscountType()==VoucherDiscountType.TOTALSALES) {
            subTotalDiscount = CalculateDiscount(VoucherDiscountType.TOTALSALES, voucher.getCalculationType(), voucher.getDiscountValue(), subTotalAmount, deliveryCharge, voucher.getMaxDiscountAmount());                        
            discount.setSubTotalDiscount(Utilities.roundDouble(subTotalDiscount,2));
        }
        
        return discount;
    }                
     
    private static double CalculateDiscount(VoucherDiscountType voucherDiscountType, DiscountCalculationType calculationType, double discountAmount,  double salesAmount, double deliveryCharge, Double maxDiscountAmt) {
        double subdiscount=0;
        if (calculationType==DiscountCalculationType.FIX) {
            subdiscount = discountAmount;
        } else if (calculationType==DiscountCalculationType.PERCENT && voucherDiscountType==VoucherDiscountType.TOTALSALES) {
            subdiscount = discountAmount / 100 * salesAmount;
        } else if (calculationType==DiscountCalculationType.PERCENT && voucherDiscountType==VoucherDiscountType.SHIPPING) {
            subdiscount = discountAmount / 100 * deliveryCharge;
        } else if (calculationType==DiscountCalculationType.SHIPAMT) {
            subdiscount = deliveryCharge;
        }
        
        if (voucherDiscountType==VoucherDiscountType.TOTALSALES && subdiscount > salesAmount ) {
            subdiscount = salesAmount;
        } else if (voucherDiscountType==VoucherDiscountType.SHIPPING && subdiscount > deliveryCharge ) {
            subdiscount = deliveryCharge;
        }
        
        if (maxDiscountAmt!=null) {
            if (maxDiscountAmt>0 && subdiscount>maxDiscountAmt) {
                subdiscount = maxDiscountAmt;
            }
        }
        
        return subdiscount;
    }
  
}
