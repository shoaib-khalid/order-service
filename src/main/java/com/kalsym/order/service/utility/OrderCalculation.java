/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.utility;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.DeliveryType;
import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.Store;
import com.kalsym.order.service.model.StoreCommission;
import com.kalsym.order.service.model.CustomerVoucher;
import com.kalsym.order.service.model.VoucherVertical;
import com.kalsym.order.service.model.object.Discount;
import com.kalsym.order.service.model.object.DiscountVoucher;
import com.kalsym.order.service.model.object.OrderObject;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.StoreDiscountRepository;
import com.kalsym.order.service.model.repository.StoreDiscountTierRepository;
import java.util.Optional;
import java.text.DecimalFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author taufik
 */
public class OrderCalculation {
    
    public static OrderObject CalculateOrderTotal(Cart cart, Double storeSvcChargePercentage, StoreCommission storeCommission, 
            Double deliveryCharge, String deliveryType, CustomerVoucher customerVoucher, String storeVerticalCode,
            CartItemRepository cartItemRepository, 
            StoreDiscountRepository storeDiscountRepository, 
            StoreDiscountTierRepository storeDiscountTierRepository, String logprefix) {
        
        OrderObject orderTotal = new OrderObject();
        if (deliveryCharge==null) { deliveryCharge=0.00; }
        //calculate Store discount
        Discount discount = StoreDiscountCalculation.CalculateStoreDiscount(cart, deliveryCharge, cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix);                
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "subTotalDiscount: " +discount.getSubTotalDiscount());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "deliveryDiscount: " +discount.getDeliveryDiscount());
        orderTotal.setAppliedDiscount(Utilities.convertToDouble(discount.getSubTotalDiscount()));
        orderTotal.setAppliedDiscountDescription(discount.getSubTotalDiscountDescription());
        orderTotal.setDeliveryDiscount(Utilities.convertToDouble(discount.getDeliveryDiscount()));
        orderTotal.setDeliveryDiscountDescription(discount.getDeliveryDiscountDescription());                
        orderTotal.setSubTotal(Utilities.convertToDouble(discount.getCartSubTotal()));
        orderTotal.setDiscountId(discount.getDiscountId());
        orderTotal.setDiscountType(discount.getDiscountType());
        orderTotal.setDiscountCalculationType(discount.getDiscountCalculationType());
        orderTotal.setDiscountCalculationValue(Utilities.convertToDouble(discount.getDiscountCalculationValue()));
        orderTotal.setDiscountMaxAmount(Utilities.convertToDouble(discount.getDiscountMaxAmount()));
        orderTotal.setDeliveryDiscountMaxAmount(Utilities.convertToDouble(discount.getDeliveryDiscountMaxAmount()));
        
        //calculate voucher code discount
        if (customerVoucher!=null) {
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "voucher minimum spend: " + customerVoucher.getVoucher().getMinimumSpend());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "voucher allowDoubleDiscount: " + customerVoucher.getVoucher().getAllowDoubleDiscount());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "voucher verticalList: " + customerVoucher.getVoucher().getVoucherVerticalList().toString());
            
            //check voucher minimum spend
            if (Utilities.convertToDouble(discount.getCartSubTotal()) < customerVoucher.getVoucher().getMinimumSpend()) {
                //error, not reach minimum amount
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Voucher cannot be used. Not reach minimum spend amount");
                return orderTotal;
            }
            
            //check voucher double discount
            if (discount.getDiscountId()!=null && customerVoucher.getVoucher().getAllowDoubleDiscount()==false) {
                //error, not allow double discount
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Voucher cannot be used. Purchase already have discount");
                return orderTotal;
            }            
            
            //check vertical code
            boolean verticalValid=false;
            for (int i=0;i<customerVoucher.getVoucher().getVoucherVerticalList().size();i++) {
                VoucherVertical voucherVertical = customerVoucher.getVoucher().getVoucherVerticalList().get(i);
                if (voucherVertical.getVerticalCode().equals(storeVerticalCode)) {
                    verticalValid=true;
                }
            }
            if (!verticalValid) {
                //error, not allow for this store
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Voucher cannot be used for this store");
                return orderTotal;
            }
            
            DiscountVoucher discountVoucher = VoucherDiscountCalculation.CalculateVoucherDiscount(cart, deliveryCharge, orderTotal.getSubTotal(), customerVoucher, logprefix);                
            double subTotalDiscount=0.00;
            double deliveryDiscount=0.00;
            if (discountVoucher.getSubTotalDiscount()!=null) {
                subTotalDiscount=discountVoucher.getSubTotalDiscount().doubleValue();
                orderTotal.setVoucherSubTotalDiscount(Utilities.convertToDouble(discountVoucher.getSubTotalDiscount()));
                orderTotal.setVoucherSubTotalDiscountDescription(discountVoucher.getSubTotalDiscountDescription());
            }
            if (discountVoucher.getDeliveryDiscount()!=null) {
                deliveryDiscount=discountVoucher.getDeliveryDiscount().doubleValue();
                orderTotal.setVoucherDeliveryDiscount(Utilities.convertToDouble(discountVoucher.getDeliveryDiscount()));
                orderTotal.setVoucherDeliveryDiscountDescription(discountVoucher.getDeliveryDiscountDescription());
            }
            
            orderTotal.setVoucherDiscountType(customerVoucher.getVoucher().getDiscountType().toString());
            orderTotal.setVoucherDiscountCalculationType(customerVoucher.getVoucher().getCalculationType().toString());
            orderTotal.setVoucherDiscountCalculationValue(customerVoucher.getVoucher().getDiscountValue());
            orderTotal.setVoucherDiscountMaxAmount(customerVoucher.getVoucher().getMaxDiscountAmount());
        
            double newSubTotal = orderTotal.getSubTotal() - subTotalDiscount;
            orderTotal.setSubTotal(newSubTotal);
            deliveryCharge = deliveryCharge - deliveryDiscount;
            
            double voucherDiscountAmount = subTotalDiscount + deliveryDiscount;
            orderTotal.setVoucherDiscount(voucherDiscountAmount);
        }
        
        //calculate Store service charge        
        double serviceCharges = calculateStoreServiceCharges(storeSvcChargePercentage, orderTotal.getSubTotal(), orderTotal.getAppliedDiscount());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "serviceCharges: " + serviceCharges);
        orderTotal.setStoreServiceCharge(serviceCharges); 
        
        //calculate grand total
        orderTotal.setTotal(orderTotal.getSubTotal() - orderTotal.getAppliedDiscount() + serviceCharges + deliveryCharge - orderTotal.getDeliveryDiscount());
        double totalWithoutDelivery = orderTotal.getSubTotal() - orderTotal.getAppliedDiscount() + serviceCharges;
                
        //calculating Kalsym commission 
        double commission = 0;
        if (storeCommission != null) {
            commission = totalWithoutDelivery * (storeCommission.getRate() / 100);
            if (commission < storeCommission.getMinChargeAmount()) {
                commission = storeCommission.getMinChargeAmount();
            }
        }
                               
        orderTotal.setKlCommission(Round2DecimalPoint(commission));
        orderTotal.setStoreShare(Round2DecimalPoint(orderTotal.getSubTotal() - orderTotal.getAppliedDiscount() + orderTotal.getStoreServiceCharge() - commission));
        
        if (deliveryType!=null) {
            if (deliveryType.equals(DeliveryType.SELF.name())) {
                double storeShare = orderTotal.getStoreShare() + deliveryCharge;
                orderTotal.setStoreShare(Round2DecimalPoint(storeShare));
            } 
        }
        
        return orderTotal;
    }
    
    
    private static Double Round2DecimalPoint(Double input) {
        if (input == null) { return null; }
        return Math.round(input * 100.0) / 100.0;
    }
    
    public static Double calculateStoreServiceCharges(Double storeSvcChargePercentage, double orderSubTotal, double orderAppliedDiscount) {
        double serviceCharges = 0;
        if (null != storeSvcChargePercentage) {
            serviceCharges = (storeSvcChargePercentage / 100) * (orderSubTotal - orderAppliedDiscount) ;            
        } 
        return serviceCharges;
    }
}
