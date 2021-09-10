/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.utility;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.DiscountCalculationType;
import com.kalsym.order.service.enums.DiscountType;
import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.StoreDiscount;
import com.kalsym.order.service.model.StoreDiscountTier;
import com.kalsym.order.service.model.object.Discount;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.StoreDiscountRepository;
import com.kalsym.order.service.model.repository.StoreDiscountTierRepository;

import java.util.Date;
import java.util.List;

/**
 *
 * @author taufik
 */
public class StoreDiscountCalculation {
    
    public static Discount CalculateStoreDiscount(Cart cart, double deliveryCharge, CartItemRepository cartItemRepository,
            StoreDiscountRepository storeDiscountRepository, StoreDiscountTierRepository storeDiscountTierRepository, String logprefix) {
         //check if any discount is active within date range
        List<StoreDiscount> discountAvailable = storeDiscountRepository.findAvailableDiscount(cart.getStoreId(), new Date());
        Discount discount = new Discount();
        if (discountAvailable.isEmpty()) {
            discount.setDeliveryDiscount(0.00);
            discount.setSubTotalDiscount(0.00);
        } else {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "discountAvailable found:"+discountAvailable.size());
            double salesAmount=0;
            List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
            for (int i=0;i<cartItems.size();i++) {
                CartItem item = cartItems.get(i);
                salesAmount = salesAmount + item.getPrice();
            }
            discount.setCartSubTotal(salesAmount);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "total sales amount:"+salesAmount);    
            double totalSubTotalDiscount=0;
            double totalShipmentDiscount=0;
            String deliveryDiscountDescription=null;
            String subTotalDiscountDescription=null;
            for (int x=0;x<discountAvailable.size();x++) {
                StoreDiscount storeDiscount = discountAvailable.get(x);                
                StoreDiscountTier discountTier = storeDiscountTierRepository.findDiscountTier(storeDiscount.getId(), salesAmount);
                double subTotalDiscount = 0;
                double shipmentDiscount = 0;
                String subDescription = "";
                if (discountTier!=null) { 
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "tier found type:"+storeDiscount.getDiscountType()+" formula:"+discountTier.getCalculationType()+" value:"+discountTier.getDiscountAmount());    
                    subDescription = GetDiscountDescription(discountTier.getCalculationType(), discountTier.getDiscountAmount(), deliveryCharge);
                    if (storeDiscount.getDiscountType().equals(DiscountType.TOTALSALES.toString())) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Calculate based on total sales");
                        subTotalDiscount = CalculateDiscount(DiscountType.TOTALSALES.toString(), discountTier.getCalculationType(), discountTier.getDiscountAmount(), salesAmount, deliveryCharge);                        
                        if (subTotalDiscountDescription==null)
                            subTotalDiscountDescription = subDescription;
                        else                            
                            subTotalDiscountDescription = subTotalDiscountDescription + "," + subDescription;
                    } else if (storeDiscount.getDiscountType().equals(DiscountType.SHIPPING.toString())) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Calculate based on shipping amount");
                        shipmentDiscount = CalculateDiscount(DiscountType.SHIPPING.toString(), discountTier.getCalculationType(), discountTier.getDiscountAmount(), salesAmount, deliveryCharge);                        
                        if (deliveryDiscountDescription==null)
                            deliveryDiscountDescription = subDescription;
                        else
                            deliveryDiscountDescription = deliveryDiscountDescription + "," + subDescription;                        
                    }
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "subTotalDiscount:"+subTotalDiscount+" shipmentDiscount:"+shipmentDiscount+" description:"+subDescription);
                } else {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "tier not found");    
                }
                totalSubTotalDiscount = totalSubTotalDiscount + subTotalDiscount;
                totalShipmentDiscount = totalShipmentDiscount + shipmentDiscount;
            }
            discount.setDeliveryDiscount(totalShipmentDiscount);
            discount.setSubTotalDiscount(totalSubTotalDiscount);
            discount.setDeliveryDiscountDescription(deliveryDiscountDescription);
            discount.setSubTotalDiscountDescription(subTotalDiscountDescription);
        }
        return discount;
    }
    
    private static double CalculateDiscount(String discountType, String calculationType, double discountTierAmount,  double salesAmount, double deliveryCharge) {
        double subdiscount=0;
        if (calculationType.equals(DiscountCalculationType.FIX.toString())) {
            subdiscount = discountTierAmount;
        } else if (calculationType.equals(DiscountCalculationType.PERCENT.toString()) && discountType.equals(DiscountType.TOTALSALES.toString())) {
            subdiscount = discountTierAmount / 100 * salesAmount;
        } else if (calculationType.equals(DiscountCalculationType.PERCENT.toString()) && discountType.equals(DiscountType.SHIPPING.toString())) {
            subdiscount = discountTierAmount / 100 * deliveryCharge;
        } else if (calculationType.equals(DiscountCalculationType.SHIPAMT.toString())) {
            subdiscount = deliveryCharge;
        }
        return subdiscount;
    }
    
    private static String GetDiscountDescription(String calculationType, double discountTierAmount, double shipAmount) {
        if (calculationType.equals(DiscountCalculationType.PERCENT.toString())) {
            return "-"+discountTierAmount+"%";
        } else if (calculationType.equals(DiscountCalculationType.FIX.toString())) {
            return "-"+discountTierAmount;
        } else if (calculationType.equals(DiscountCalculationType.SHIPAMT.toString())) {
            return "-"+shipAmount;
        } else {
            return "";
        }
    }
}
