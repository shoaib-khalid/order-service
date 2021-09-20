/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.utility;

import com.kalsym.order.service.OrderServiceApplication;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author taufik
 */
public class OrderCalculation {
    
    public static OrderObject CalculateOrderTotal(Cart cart, Order order, Double storeSvcChargePercentage, StoreCommission storeCommission,
            CartItemRepository cartItemRepository, 
            StoreDiscountRepository storeDiscountRepository, 
            StoreDiscountTierRepository storeDiscountTierRepository, String logprefix) {
        
        OrderObject orderTotal = new OrderObject();
        
        //calculate Store discount
        Discount discount = StoreDiscountCalculation.CalculateStoreDiscount(cart, order.getDeliveryCharges(), cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix);                
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "subTotalDiscount: " +discount.getSubTotalDiscount());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "deliveryDiscount: " +discount.getDeliveryDiscount());
        orderTotal.setAppliedDiscount(discount.getSubTotalDiscount());
        orderTotal.setAppliedDiscountDescription(discount.getSubTotalDiscountDescription());
        orderTotal.setDeliveryDiscount(discount.getDeliveryDiscount());
        orderTotal.setDeliveryDiscountDescription(discount.getDeliveryDiscountDescription());                
        orderTotal.setSubTotal(discount.getCartSubTotal());
        
        //calculate Store service charge
        double serviceCharges = 0;
        if (null != storeSvcChargePercentage) {
            serviceCharges = (storeSvcChargePercentage / 100) * (orderTotal.getSubTotal() - orderTotal.getAppliedDiscount()) ;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "serviceCharges: " + serviceCharges);
        }
        orderTotal.setStoreServiceCharge(serviceCharges); 
        
        //calculate grand total
        orderTotal.setTotal(orderTotal.getSubTotal() - orderTotal.getAppliedDiscount() + serviceCharges + order.getDeliveryCharges() - orderTotal.getDeliveryDiscount());

        //calculating Kalsym commission 
        double commission = 0;
        if (storeCommission != null) {
            commission = order.getTotal() * (storeCommission.getRate() / 100);
            if (commission < storeCommission.getMinChargeAmount()) {
                commission = storeCommission.getMinChargeAmount();
            }
        }
                               
        orderTotal.setKlCommission(commission);
        orderTotal.setStoreShare(orderTotal.getSubTotal() - orderTotal.getAppliedDiscount() + orderTotal.getStoreServiceCharge() - commission);
        
        return orderTotal;
    }
}
