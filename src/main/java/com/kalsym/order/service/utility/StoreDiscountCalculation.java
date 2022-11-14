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
import com.kalsym.order.service.model.CartItemAddOn;
import com.kalsym.order.service.model.StoreDiscount;
import com.kalsym.order.service.model.StoreDiscountTier;
import com.kalsym.order.service.model.object.Discount;
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
public class StoreDiscountCalculation {
    
    public static Discount CalculateStoreDiscount(Cart cart, double deliveryCharge, CartItemRepository cartItemRepository,
            StoreDiscountRepository storeDiscountRepository, StoreDiscountTierRepository storeDiscountTierRepository, String logprefix, List<CartItem> selectedCartItem) {
         //check if any discount is active within date range
        List<StoreDiscount> discountAvailable = storeDiscountRepository.findAvailableDiscount(cart.getStoreId(), new Date());
        Discount discount = new Discount();
        if (discountAvailable.isEmpty()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "discountAvailable not found");
            discount.setDeliveryDiscount(Utilities.roundDouble(0.00,2));
            discount.setSubTotalDiscount(Utilities.roundDouble(0.00,2));
            double salesAmount=0;
            List<CartItem> cartItems = null;
            if (selectedCartItem!=null) {
                cartItems = selectedCartItem;
            } else {
                cartItems = cartItemRepository.findByCartId(cart.getId());
            }
            for (int i=0;i<cartItems.size();i++) {
                CartItem item = cartItems.get(i);
                salesAmount = salesAmount + item.getPrice();
                //check cart addOn item
                if (item.getCartItemAddOn()!=null && !item.getCartItemAddOn().isEmpty()) {
                    for (int x=0;x<item.getCartItemAddOn().size();x++) {
                        CartItemAddOn cartItemAddOn = item.getCartItemAddOn().get(x);
                        salesAmount = salesAmount + cartItemAddOn.getPrice();
                    }
                }
            }
            discount.setCartSubTotal(Utilities.roundDouble(salesAmount,2));
        } else {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "discountAvailable found:"+discountAvailable.size());
            double salesAmount=0;
            double salesDiscountedItem=0;
            List<CartItem> cartItems = null;
            if (selectedCartItem!=null) {
                cartItems = selectedCartItem;
            } else {
                cartItems = cartItemRepository.findByCartId(cart.getId());
            }
            for (int i=0;i<cartItems.size();i++) {
                CartItem item = cartItems.get(i);
                //check if item already discounted item 
                if (item.getDiscountId()!=null) {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Item "+item.getItemCode()+" already discounted price:"+item.getPrice());
                    salesDiscountedItem = salesDiscountedItem + item.getPrice();
                }
                salesAmount = salesAmount + item.getPrice();
                //check cart addOn item
                if (item.getCartItemAddOn()!=null && !item.getCartItemAddOn().isEmpty()) {
                    for (int x=0;x<item.getCartItemAddOn().size();x++) {
                        CartItemAddOn cartItemAddOn = item.getCartItemAddOn().get(x);
                        salesAmount = salesAmount + cartItemAddOn.getPrice();
                    }
                }
                
            }
            discount.setCartSubTotal(Utilities.roundDouble(salesAmount,2));
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
                    
                    if (storeDiscount.getDiscountType().equals(DiscountType.TOTALSALES.toString())) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Calculate based on total sales. Normal item only:"+storeDiscount.getNormalPriceItemOnly());
                        double eligibleSalesAmount=0;
                        if (storeDiscount.getNormalPriceItemOnly()) {
                            eligibleSalesAmount = salesAmount - salesDiscountedItem;
                        } else {
                            eligibleSalesAmount = salesAmount;
                        }
                        subTotalDiscount = CalculateDiscount(DiscountType.TOTALSALES.toString(), discountTier.getCalculationType(), discountTier.getDiscountAmount(), eligibleSalesAmount, deliveryCharge, storeDiscount.getMaxDiscountAmount());                        
                        subDescription = GetSubTotalDiscountDescription(discountTier.getCalculationType(), discountTier.getDiscountAmount(), subTotalDiscount);
                        discount.setDiscountMaxAmount(Utilities.roundDouble(storeDiscount.getMaxDiscountAmount(),2));
                        if (subTotalDiscountDescription==null)
                            subTotalDiscountDescription = subDescription;
                        else                            
                            subTotalDiscountDescription = subTotalDiscountDescription + "," + subDescription;
                    
                    } else if (storeDiscount.getDiscountType().equals(DiscountType.SHIPPING.toString())) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Calculate based on shipping amount");
                        shipmentDiscount = CalculateDiscount(DiscountType.SHIPPING.toString(), discountTier.getCalculationType(), discountTier.getDiscountAmount(), salesAmount, deliveryCharge, storeDiscount.getMaxDiscountAmount());                        
                        subDescription = GetShipmentDiscountDescription(discountTier.getCalculationType(), discountTier.getDiscountAmount(), shipmentDiscount);
                        discount.setDeliveryDiscountMaxAmount(Utilities.roundDouble(storeDiscount.getMaxDiscountAmount(),2));
                        if (deliveryDiscountDescription==null)
                            deliveryDiscountDescription = subDescription;
                        else
                            deliveryDiscountDescription = deliveryDiscountDescription + "," + subDescription;                        
                    }
                    discount.setDiscountType(storeDiscount.getDiscountType());
                    discount.setDiscountCalculationType(discountTier.getCalculationType());
                    discount.setDiscountCalculationValue(Utilities.roundDouble(discountTier.getDiscountAmount(),2));
                    discount.setDiscountId(storeDiscount.getId());
                    
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "subTotalDiscount:"+subTotalDiscount+" shipmentDiscount:"+shipmentDiscount+" description:"+subDescription);
                } else {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "tier not found");    
                }
                totalSubTotalDiscount = totalSubTotalDiscount + subTotalDiscount;
                totalShipmentDiscount = totalShipmentDiscount + shipmentDiscount;
            }
            discount.setDeliveryDiscount(Utilities.roundDouble(totalShipmentDiscount,2));
            discount.setSubTotalDiscount(Utilities.roundDouble(totalSubTotalDiscount,2));
            discount.setDeliveryDiscountDescription(deliveryDiscountDescription);
            discount.setSubTotalDiscountDescription(subTotalDiscountDescription);
        }
        return discount;
    }                
     
    private static double CalculateDiscount(String discountType, String calculationType, double discountTierAmount,  double salesAmount, double deliveryCharge, Double maxDiscountAmt) {
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
        
        if (discountType.equals(DiscountType.TOTALSALES.toString()) && subdiscount > salesAmount ) {
            subdiscount = salesAmount;
        } else if (discountType.equals(DiscountType.SHIPPING.toString()) && subdiscount > deliveryCharge ) {
            subdiscount = deliveryCharge;
        }
        
        if (maxDiscountAmt!=null) {
            if (maxDiscountAmt>0 && subdiscount>maxDiscountAmt) {
                subdiscount = maxDiscountAmt;
            }
        }
        
        return subdiscount;
    }
    
    private static String GetSubTotalDiscountDescription(String calculationType, double discountTierAmount, double discountAmount) {
        if (calculationType.equals(DiscountCalculationType.PERCENT.toString())) {
            return "-"+discountTierAmount+"%";
        } else if (calculationType.equals(DiscountCalculationType.FIX.toString())) {
            return "-"+discountAmount;
        } else {
            return "";
        }
    }
    
    private static String GetShipmentDiscountDescription(String calculationType, double discountTierAmount, double discountAmount) {
        if (calculationType.equals(DiscountCalculationType.PERCENT.toString())) {
            return "-"+discountTierAmount+"%";
        } else if (calculationType.equals(DiscountCalculationType.FIX.toString())) {
            return "-"+discountAmount;
        } else if (calculationType.equals(DiscountCalculationType.SHIPAMT.toString())) {
            return "-"+discountAmount;
        } else {
            return "";
        }
    }
}
