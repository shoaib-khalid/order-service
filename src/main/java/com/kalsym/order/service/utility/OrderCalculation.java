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
import com.kalsym.order.service.model.object.Discount;
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
            Double deliveryCharge, String deliveryType,
            CartItemRepository cartItemRepository, 
            StoreDiscountRepository storeDiscountRepository, 
            StoreDiscountTierRepository storeDiscountTierRepository, String logprefix) {
        
        OrderObject orderTotal = new OrderObject();
        
        //calculate Store discount
        Discount discount = StoreDiscountCalculation.CalculateStoreDiscount(cart, deliveryCharge, cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix);                
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "subTotalDiscount: " +discount.getSubTotalDiscount());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "deliveryDiscount: " +discount.getDeliveryDiscount());
        orderTotal.setAppliedDiscount(Round2DecimalPoint(discount.getSubTotalDiscount()));
        orderTotal.setAppliedDiscountDescription(discount.getSubTotalDiscountDescription());
        orderTotal.setDeliveryDiscount(Round2DecimalPoint(discount.getDeliveryDiscount()));
        orderTotal.setDeliveryDiscountDescription(discount.getDeliveryDiscountDescription());                
        orderTotal.setSubTotal(Round2DecimalPoint(discount.getCartSubTotal()));
        orderTotal.setDiscountId(discount.getDiscountId());
        orderTotal.setDiscountType(discount.getDiscountType());
        orderTotal.setDiscountCalculationType(discount.getDiscountCalculationType());
        orderTotal.setDiscountCalculationValue(Round2DecimalPoint(discount.getDiscountCalculationValue()));
        orderTotal.setDiscountMaxAmount(Round2DecimalPoint(discount.getDiscountMaxAmount()));
        orderTotal.setDeliveryDiscountMaxAmount(Round2DecimalPoint(discount.getDeliveryDiscountMaxAmount()));
        
        //calculate Store service charge
        double serviceCharges = 0;
        if (null != storeSvcChargePercentage) {
            serviceCharges = (storeSvcChargePercentage / 100) * (orderTotal.getSubTotal() - orderTotal.getAppliedDiscount()) ;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "serviceCharges: " + serviceCharges);
        }
        orderTotal.setStoreServiceCharge(serviceCharges); 
        
        //calculate grand total
        orderTotal.setTotal(orderTotal.getSubTotal() - orderTotal.getAppliedDiscount() + serviceCharges + deliveryCharge - orderTotal.getDeliveryDiscount());

        //calculating Kalsym commission 
        double commission = 0;
        if (storeCommission != null) {
            commission = orderTotal.getTotal() * (storeCommission.getRate() / 100);
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
}
