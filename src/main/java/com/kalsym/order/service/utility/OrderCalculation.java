/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.utility;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.DeliveryType;
import com.kalsym.order.service.enums.ServiceType;
import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.Store;
import com.kalsym.order.service.model.StoreCommission;
import com.kalsym.order.service.model.CustomerVoucher;
import com.kalsym.order.service.model.VoucherVertical;
import com.kalsym.order.service.model.VoucherStore;
import com.kalsym.order.service.model.VoucherServiceType;
import com.kalsym.order.service.model.object.Discount;
import com.kalsym.order.service.model.object.DiscountVoucher;
import com.kalsym.order.service.model.object.OrderObject;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.StoreDiscountRepository;
import com.kalsym.order.service.model.repository.StoreDiscountTierRepository;
import java.util.Optional;
import java.util.List;
import java.text.DecimalFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author taufik
 */
public class OrderCalculation {
    
    public static OrderObject CalculateOrderTotal(Cart cart,
            Double storeSvcChargePercentage, StoreCommission storeCommission,
            Double deliveryCharge, String deliveryType, 
            CustomerVoucher customerPlatformVoucher,
            CustomerVoucher customerStoreVoucher, String storeVerticalCode,
            CartItemRepository cartItemRepository, 
            StoreDiscountRepository storeDiscountRepository, 
            StoreDiscountTierRepository storeDiscountTierRepository, String logprefix,
            List<CartItem> selectedCartItemList) {
        
        OrderObject orderTotal = new OrderObject();
        orderTotal.setGotError(Boolean.FALSE);
        if (deliveryCharge==null) { deliveryCharge=0.00; }
        //calculate Store discount
        Discount discount = StoreDiscountCalculation.CalculateStoreDiscount(cart, deliveryCharge, cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix, selectedCartItemList);                
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
        
        boolean gotItemDiscount=false;
        //check if got item discount inside cart
        for (int x=0;x<selectedCartItemList.size();x++) {
            CartItem cartItem = selectedCartItemList.get(x);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "CartItem discount:"+cartItem.getDiscountId());
            if (cartItem.getDiscountId()!=null) {
                gotItemDiscount=true;
                break;
            }
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Got item dicount:"+gotItemDiscount+" customerPlatformVoucher:"+customerPlatformVoucher+" customerStoreVoucher:"+customerStoreVoucher);
                
        //calculate platform voucher code discount
        double platformVoucherDiscountAmount = 0.00;
        if (customerPlatformVoucher!=null) {
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Platform voucher minimum spend: " + customerPlatformVoucher.getVoucher().getMinimumSpend());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Platform voucher allowDoubleDiscount: " + customerPlatformVoucher.getVoucher().getAllowDoubleDiscount());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Platform voucher verticalList: " + customerPlatformVoucher.getVoucher().getVoucherVerticalList().toString());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Platform voucher serviceType: " + customerPlatformVoucher.getVoucher().getVoucherServiceTypeList().toString());
            
            //check voucher minimum spend
            if (Utilities.convertToDouble(discount.getCartSubTotal()) < customerPlatformVoucher.getVoucher().getMinimumSpend()) {
                //error, not reach minimum amount
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Your order did not meet the minimum spend required of the voucher.");
                return orderTotal;
            }
            
            //check voucher double discount
            if (discount.getDiscountId()!=null && customerPlatformVoucher.getVoucher().getAllowDoubleDiscount()==false) {
                //error, not allow double discount
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Sorry, this voucher is not applicable for product with on-going campaign.");
                return orderTotal;
            }  
            
            //check voucher double discount for item discount
            if (gotItemDiscount && customerPlatformVoucher.getVoucher().getAllowDoubleDiscount()==false) {
                //error, not allow double discount
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Sorry, this voucher is not applicable for product with on-going campaign.");
                return orderTotal;
            }   
            
            //check vertical code
            boolean verticalValid=false;
            for (int i=0;i<customerPlatformVoucher.getVoucher().getVoucherVerticalList().size();i++) {
                VoucherVertical voucherVertical = customerPlatformVoucher.getVoucher().getVoucherVerticalList().get(i);
                if (voucherVertical.getVerticalCode().equals(storeVerticalCode)) {
                    verticalValid=true;
                }
            }
            if (!verticalValid) {
                //error, not allow for this store
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Voucher cannot be used for this store.");
                return orderTotal;
            }    
            
            //check serviceType 
            boolean serviceTypeValid=false;
            if (customerPlatformVoucher.getVoucher().getVoucherServiceTypeList()!=null && !customerPlatformVoucher.getVoucher().getVoucherServiceTypeList().isEmpty()) {
                for (int i=0;i<customerPlatformVoucher.getVoucher().getVoucherServiceTypeList().size();i++) {
                    VoucherServiceType voucherServiceType = customerPlatformVoucher.getVoucher().getVoucherServiceTypeList().get(i);
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION,
                            logprefix, "voucherServiceType:"+voucherServiceType.getServiceType().name()+" cartServiceType:"+cart.getServiceType());
                    if (voucherServiceType.getServiceType().equals(cart.getServiceType())) {
                        serviceTypeValid=true;
                    }
                }
            } else {
                serviceTypeValid=true;
            }            
            if (!serviceTypeValid) {
                //error, not allow for this store
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Voucher cannot be used for this service.");
                return orderTotal;
            }    
            
            DiscountVoucher discountVoucher = VoucherDiscountCalculation.CalculateVoucherDiscount( deliveryCharge, orderTotal.getSubTotal(), customerPlatformVoucher, logprefix);                
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
            
            orderTotal.setVoucherDiscountType(customerPlatformVoucher.getVoucher().getDiscountType().toString());
            orderTotal.setVoucherDiscountCalculationType(customerPlatformVoucher.getVoucher().getCalculationType().toString());
            orderTotal.setVoucherDiscountCalculationValue(customerPlatformVoucher.getVoucher().getDiscountValue());
            orderTotal.setVoucherDiscountMaxAmount(customerPlatformVoucher.getVoucher().getMaxDiscountAmount());
        
            deliveryCharge = deliveryCharge - deliveryDiscount;
            
            platformVoucherDiscountAmount = subTotalDiscount + deliveryDiscount;
            orderTotal.setVoucherDiscount(platformVoucherDiscountAmount);
            orderTotal.setVoucherId(customerPlatformVoucher.getVoucher().getId());
        }
        
        //calculate store voucher discount
        double storeVoucherDiscountAmount = 0.00;
        if (customerStoreVoucher!=null) {
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store voucher minimum spend: " + customerStoreVoucher.getVoucher().getMinimumSpend());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store voucher allowDoubleDiscount: " + customerStoreVoucher.getVoucher().getAllowDoubleDiscount());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store voucher verticalList: " + customerStoreVoucher.getVoucher().getVoucherVerticalList().toString());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store voucher serviceTypeList: " + customerStoreVoucher.getVoucher().getVoucherServiceTypeList().toString());
            
            //check voucher minimum spend
            if (Utilities.convertToDouble(discount.getCartSubTotal()) < customerStoreVoucher.getVoucher().getMinimumSpend()) {
                //error, not reach minimum amount
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Your order did not meet the minimum spend required of the voucher.");
                return orderTotal;
            }
            
            //check voucher double discount
            if (discount.getDiscountId()!=null && customerStoreVoucher.getVoucher().getAllowDoubleDiscount()==false) {
                //error, not allow double discount
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Sorry, this voucher is not applicable for product with on-going campaign.");
                return orderTotal;
            }  
            
            //check voucher double discount for item discount
            if (gotItemDiscount && customerStoreVoucher.getVoucher().getAllowDoubleDiscount()==false) {
                //error, not allow double discount
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Sorry, this voucher is not applicable for product with on-going campaign.");
                return orderTotal;
            } 
            
            //check vertical code
            boolean verticalValid=false;
            for (int i=0;i<customerStoreVoucher.getVoucher().getVoucherVerticalList().size();i++) {
                VoucherVertical voucherVertical = customerStoreVoucher.getVoucher().getVoucherVerticalList().get(i);
                if (voucherVertical.getVerticalCode().equals(storeVerticalCode)) {
                    verticalValid=true;
                }
            }
            if (!verticalValid) {
                //error, not allow for this store
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Voucher cannot be used for this store.");
                return orderTotal;
            }
            
             //check serviceType 
            boolean serviceTypeValid=false;
            if (customerStoreVoucher.getVoucher().getVoucherServiceTypeList()!=null && customerStoreVoucher.getVoucher().getVoucherServiceTypeList().size()>0) {
                for (int i=0;i<customerStoreVoucher.getVoucher().getVoucherServiceTypeList().size();i++) {
                    VoucherServiceType voucherServiceType = customerStoreVoucher.getVoucher().getVoucherServiceTypeList().get(i);
                    if (voucherServiceType.getServiceType().equals(cart.getServiceType())) {
                        serviceTypeValid=true;
                    }
                }
            } else {
                serviceTypeValid=true;
            }
            if (!serviceTypeValid) {
                //error, not allow for this store
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Voucher cannot be used for this service.");
                return orderTotal;
            }    
            
            //check store            
            boolean storeValid=false;
            for (int i=0;i<customerStoreVoucher.getVoucher().getVoucherStoreList().size();i++) {
                VoucherStore voucherStore = customerStoreVoucher.getVoucher().getVoucherStoreList().get(i);
                if (voucherStore.getStoreId().equals(cart.getStoreId())) {
                    storeValid=true;
                }
            }
            if (!storeValid) {
                //error, not allow for this store
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Voucher cannot be used for this store.");
                return orderTotal;
            }           
            
            DiscountVoucher discountVoucher = VoucherDiscountCalculation.CalculateVoucherDiscount(deliveryCharge, orderTotal.getSubTotal(), customerStoreVoucher, logprefix);                
            double subTotalDiscount=0.00;
            double deliveryDiscount=0.00;
            if (discountVoucher.getSubTotalDiscount()!=null) {
                subTotalDiscount=discountVoucher.getSubTotalDiscount().doubleValue();
                orderTotal.setStoreVoucherSubTotalDiscount(Utilities.convertToDouble(discountVoucher.getSubTotalDiscount()));
                orderTotal.setStoreVoucherSubTotalDiscountDescription(discountVoucher.getSubTotalDiscountDescription());
            }
            if (discountVoucher.getDeliveryDiscount()!=null) {
                deliveryDiscount=discountVoucher.getDeliveryDiscount().doubleValue();
                orderTotal.setStoreVoucherDeliveryDiscount(Utilities.convertToDouble(discountVoucher.getDeliveryDiscount()));
                orderTotal.setStoreVoucherDeliveryDiscountDescription(discountVoucher.getDeliveryDiscountDescription());
            }
            
            orderTotal.setStoreVoucherDiscountType(customerStoreVoucher.getVoucher().getDiscountType().toString());
            orderTotal.setStoreVoucherDiscountCalculationType(customerStoreVoucher.getVoucher().getCalculationType().toString());
            orderTotal.setStoreVoucherDiscountCalculationValue(customerStoreVoucher.getVoucher().getDiscountValue());
            orderTotal.setStoreVoucherDiscountMaxAmount(customerStoreVoucher.getVoucher().getMaxDiscountAmount());
        
            deliveryCharge = deliveryCharge - deliveryDiscount;
            
            storeVoucherDiscountAmount = subTotalDiscount + deliveryDiscount;
            orderTotal.setStoreVoucherDiscount(storeVoucherDiscountAmount);
            orderTotal.setStoreVoucherId(customerStoreVoucher.getVoucher().getId());
        }
        
        //calculate Store service charge        
        double serviceCharges = calculateStoreServiceCharges(storeSvcChargePercentage, orderTotal.getSubTotal(), orderTotal.getAppliedDiscount());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "serviceCharges: " + serviceCharges);
        orderTotal.setStoreServiceCharge(serviceCharges); 
        
        //calculate grand total
        orderTotal.setTotal(orderTotal.getSubTotal() - orderTotal.getAppliedDiscount() + serviceCharges + deliveryCharge - orderTotal.getDeliveryDiscount() - platformVoucherDiscountAmount - storeVoucherDiscountAmount);
        double totalWithoutDelivery = orderTotal.getSubTotal() - orderTotal.getAppliedDiscount() + serviceCharges;
                
        //calculating Kalsym commission 
        double commission = 0;
        if (storeCommission != null) {                                    
            if (cart.getServiceType()!=null && cart.getServiceType()==ServiceType.DINEIN) {
                //no commission for DINE-IN
                commission = 0;
            } else {
                commission = totalWithoutDelivery * (storeCommission.getRate() / 100);
                if (commission < storeCommission.getMinChargeAmount()) {
                    commission = storeCommission.getMinChargeAmount();
                }    
            }
        }
                               
        orderTotal.setKlCommission(Round2DecimalPoint(commission));
        orderTotal.setStoreShare(Round2DecimalPoint(orderTotal.getSubTotal() - orderTotal.getAppliedDiscount() + orderTotal.getStoreServiceCharge() - commission - storeVoucherDiscountAmount));
        
        if (deliveryType!=null) {
            if (deliveryType.equals(DeliveryType.SELF.toString())) {
                double storeShare = orderTotal.getStoreShare() + deliveryCharge;
                orderTotal.setStoreShare(Round2DecimalPoint(storeShare));
            } 
        }
        
        return orderTotal;
    }
    
    
    public static OrderObject CalculateGroupOrderTotal(
            Double sumCartSubTotal, Double sumCartSubTotalDiscount,
            Double sumDeliveryCharge, Double sumDeliveryChargeDiscount,
            CustomerVoucher platformVoucher, Double sumServiceCharge,
            String logprefix, boolean gotItemDiscount, 
            String cartVerticalCode, String cartServiceType) {
        
        OrderObject orderTotal = new OrderObject();
        orderTotal.setGotError(Boolean.FALSE);
        if (sumDeliveryCharge==null) { sumDeliveryCharge=0.00; }
        
        //calculate voucher code discount
        double voucherDiscountAmount = 0.00;
        if (platformVoucher!=null) {
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Platform voucher minimum spend: " + platformVoucher.getVoucher().getMinimumSpend());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Platform voucher allowDoubleDiscount: " + platformVoucher.getVoucher().getAllowDoubleDiscount());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Platform voucher verticalList: " + platformVoucher.getVoucher().getVoucherVerticalList().toString());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Platform voucher serviceType: " + platformVoucher.getVoucher().getVoucherServiceTypeList().toString());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartVerticalCode: " +cartVerticalCode+" cartServiceType:"+cartServiceType);
            
            //check voucher minimum spend
            if (sumCartSubTotal < platformVoucher.getVoucher().getMinimumSpend()) {
                //error, not reach minimum amount
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error. sumCartSubTotal:"+sumCartSubTotal+" minimum spend:"+platformVoucher.getVoucher().getMinimumSpend());
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Your order did not meet the minimum spend required of the voucher.");
                return orderTotal;
            }
            
            //check voucher double discount (sub total)
            if (platformVoucher.getVoucher().getAllowDoubleDiscount()==false && sumCartSubTotalDiscount>0) {
                //error, not allow double discount
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error. AllowDoubleDiscount:"+platformVoucher.getVoucher().getAllowDoubleDiscount()+" sumCartSubTotalDiscount:"+sumCartSubTotalDiscount);
                orderTotal.setGotError(Boolean.TRUE);                
                orderTotal.setErrorMessage("Sorry, this voucher is not applicable for product with on-going campaign.");                
                return orderTotal;
            }
            
             //check voucher double discount (delivery discount)
            if (platformVoucher.getVoucher().getAllowDoubleDiscount()==false && sumDeliveryChargeDiscount>0) {
                //error, not allow double discount
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error. AllowDoubleDiscount:"+platformVoucher.getVoucher().getAllowDoubleDiscount()+" sumDeliveryChargeDiscount:"+sumDeliveryChargeDiscount);                
                orderTotal.setGotError(Boolean.TRUE);                
                orderTotal.setErrorMessage("Sorry, this voucher is not applicable for product with on-going campaign.");                
                return orderTotal;
            }
            
             //check voucher double discount for item discount
            if (gotItemDiscount && platformVoucher.getVoucher().getAllowDoubleDiscount()==false) {
                //error, not allow double discount
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error. AllowDoubleDiscount:"+platformVoucher.getVoucher().getAllowDoubleDiscount()+" gotItemDiscount:"+gotItemDiscount);                               
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Sorry, this voucher is not applicable for product with on-going campaign.");
                return orderTotal;
            } 
            
            //check vertical code
            boolean verticalValid=false;
            for (int i=0;i<platformVoucher.getVoucher().getVoucherVerticalList().size();i++) {
                VoucherVertical voucherVertical = platformVoucher.getVoucher().getVoucherVerticalList().get(i);
                if (voucherVertical.getVerticalCode().equals(cartVerticalCode)) {
                    verticalValid=true;
                }
            }
            if (!verticalValid) {
                //error, not allow for this store
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error. verticalValid:"+verticalValid);
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Sorry, this voucher cannot be used for this store.");
                return orderTotal;
            }    
            
            //check serviceType 
            boolean serviceTypeValid=false;
            if (platformVoucher.getVoucher().getVoucherServiceTypeList()!=null && platformVoucher.getVoucher().getVoucherServiceTypeList().size()>0) {
                for (int i=0;i<platformVoucher.getVoucher().getVoucherServiceTypeList().size();i++) {
                    VoucherServiceType voucherServiceType = platformVoucher.getVoucher().getVoucherServiceTypeList().get(i);
                    if (voucherServiceType.getServiceType().name().equals(cartServiceType)) {
                        serviceTypeValid=true;
                    }
                }
            } else {
                serviceTypeValid=true;
            }            
            if (!serviceTypeValid) {
                //error, not allow for this store
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error. serviceTypeValid:"+serviceTypeValid);
                orderTotal.setGotError(Boolean.TRUE);
                orderTotal.setErrorMessage("Sorry, this voucher cannot be used for this service.");
                return orderTotal;
            }    
            
            DiscountVoucher discountVoucher = VoucherDiscountCalculation.CalculateVoucherDiscount( sumDeliveryCharge, sumCartSubTotal, platformVoucher, logprefix);                
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
            
            orderTotal.setVoucherDiscountType(platformVoucher.getVoucher().getDiscountType().toString());
            orderTotal.setVoucherDiscountCalculationType(platformVoucher.getVoucher().getCalculationType().toString());
            orderTotal.setVoucherDiscountCalculationValue(platformVoucher.getVoucher().getDiscountValue());
            orderTotal.setVoucherDiscountMaxAmount(platformVoucher.getVoucher().getMaxDiscountAmount());
         
            voucherDiscountAmount = subTotalDiscount + deliveryDiscount;
            orderTotal.setVoucherDiscount(voucherDiscountAmount);
            orderTotal.setVoucherId(platformVoucher.getVoucher().getId());
        }
        
       
        //calculate grand total
        orderTotal.setTotal(sumCartSubTotal - sumCartSubTotalDiscount + sumDeliveryCharge - sumDeliveryChargeDiscount - voucherDiscountAmount + sumServiceCharge);
        
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
