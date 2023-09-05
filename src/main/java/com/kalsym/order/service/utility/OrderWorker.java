/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.utility;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.DeliveryType;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.enums.ProductStatus;
import com.kalsym.order.service.enums.StorePaymentType;
import com.kalsym.order.service.enums.VehicleType;
import com.kalsym.order.service.enums.ServiceType;
import com.kalsym.order.service.enums.Channel;
import com.kalsym.order.service.model.Body;
import com.kalsym.order.service.model.COD;
import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.CartSubItem;
import com.kalsym.order.service.model.Customer;
import com.kalsym.order.service.model.CustomerVoucher;
import com.kalsym.order.service.model.DeliveryQuotation;
import com.kalsym.order.service.model.Email;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderCompletionStatusConfig;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderPaymentDetail;
import com.kalsym.order.service.model.OrderSubItem;
import com.kalsym.order.service.model.OrderItemAddOn;
import com.kalsym.order.service.model.Product;
import com.kalsym.order.service.model.ProductAddOn;
import com.kalsym.order.service.model.ProductInventory;
import com.kalsym.order.service.model.ProductInventoryItem;
import com.kalsym.order.service.model.ProductVariantAvailable;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.StoreCommission;
import com.kalsym.order.service.model.StoreDeliveryDetail;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.object.ItemDiscount;
import com.kalsym.order.service.model.object.OrderObject;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.CartRepository;
import com.kalsym.order.service.model.repository.CustomerVoucherRepository;
import com.kalsym.order.service.model.repository.StoreDetailsRepository;
import com.kalsym.order.service.model.repository.StoreDeliveryDetailRepository;
import com.kalsym.order.service.model.repository.ProductInventoryRepository;
import com.kalsym.order.service.model.repository.StoreDiscountRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.model.repository.StoreDiscountTierRepository;
import com.kalsym.order.service.model.repository.OrderPaymentDetailRepository;
import com.kalsym.order.service.model.repository.VoucherRepository;
import com.kalsym.order.service.model.repository.OrderShipmentDetailRepository;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.OrderSubItemRepository;
import com.kalsym.order.service.model.repository.OrderItemAddOnRepository;
import com.kalsym.order.service.model.repository.StoreRepository;
import com.kalsym.order.service.model.repository.RegionCountriesRepository;
import com.kalsym.order.service.model.repository.CustomerRepository;
import com.kalsym.order.service.model.repository.OrderCompletionStatusConfigRepository;
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.service.OrderPostService;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.service.EmailService;
import com.kalsym.order.service.service.FCMService;
import com.kalsym.order.service.service.CustomerService;
import com.kalsym.order.service.service.WhatsappService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author taufik
 */
public class OrderWorker {
    
    public static HttpResponse placeOrder(
            String requestUri,
            Cart cart,
            List<CartItem> cartItems,
            COD cod,
            StoreWithDetails storeWithDetials,
            StoreDeliveryDetail storeDeliveryDetail,
            CustomerVoucher customerStoreVoucher,
            Boolean saveCustomerInformation,
            Boolean sendReceiptToReceiver,  
            Channel channel,
            String tableNo,
            String zone,
            String paymentChannel,
            String staffId,
            String onboardingOrderLink,
            String invoiceBaseUrl,
            String logprefix,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            CustomerVoucherRepository customerVoucherRepository,
            StoreDetailsRepository storeDetailsRepository,
            StoreDeliveryDetailRepository storeDeliveryDetailRepository,
            ProductInventoryRepository productInventoryRepository,
            StoreDiscountRepository storeDiscountRepository,
            StoreDiscountTierRepository storeDiscountTierRepository,
            OrderRepository orderRepository,
            OrderPaymentDetailRepository orderPaymentDetailRepository,
            OrderShipmentDetailRepository orderShipmentDetailRepository,
            OrderItemRepository orderItemRepository,
            OrderSubItemRepository orderSubItemRepository,
            OrderItemAddOnRepository orderItemAddOnRepository,
            VoucherRepository voucherRepository,
            StoreRepository storeRepository,
            RegionCountriesRepository regionCountriesRepository,
            CustomerRepository customerRepository,
            OrderCompletionStatusConfigRepository orderCompletionStatusConfigRepository,
            ProductService productService,
            OrderPostService orderPostService,
            FCMService fcmService,
            EmailService emailService,
            DeliveryService deliveryService,
            CustomerService customerService,
            WhatsappService whatsappService,
            String assetServiceBaseUrl) {

        HttpResponse response = new HttpResponse(requestUri);
        // create order object
        Order order = new Order();
        String cartId = cart.getId();
        
        try {

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart exists against cartId: " + cartId);          
            
            StoreCommission storeCommission = productService.getStoreCommissionByStoreId(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store commission: " + storeCommission);

            Double subTotal = 0.0;
            List<OrderItem> orderItems = new ArrayList<OrderItem>();
            try {
                // check store payment type
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store with payment type: " + storeWithDetials.getPaymentType());

                order.setStoreId(storeWithDetials.getId());
                                                    
                for (int i = 0; i < cartItems.size(); i++) {
                    // check every items price in product service
                    ProductInventory productInventory = productService.getProductInventoryById(cart.getStoreId(), cartItems.get(i).getProductId(), cartItems.get(i).getItemCode());
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got productinventory against itemcode:" + cartItems.get(i).getItemCode());
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got productinventory: " + cartItems.get(i).getItemCode(), productInventory);

                    //get product variant
                    ProductInventory productInventoryDB = productInventoryRepository.findByItemCode(cartItems.get(i).getItemCode());       
                    String variantList = null;
                    if (productInventory.getProductInventoryItems().size()>0) {
                        for (int x=0;x<productInventoryDB.getProductInventoryItems().size();x++) {
                            ProductInventoryItem productInventoryItem = productInventory.getProductInventoryItems().get(x);
                            ProductVariantAvailable productVariantAvailable = productInventoryItem.getProductVariantAvailable();
                            String variant = productVariantAvailable.getValue();
                            if (variantList==null)
                                variantList = variant;
                            else
                                variantList = variantList + "," + variant;
                        }
                    }
                    
                    //check for stock
                    if (productInventory.getQuantity()<cartItems.get(i).getQuantity() && productInventory.getProduct().isAllowOutOfStockPurchases()==false) {
                        //out of stock
                        response.setMessage(productInventory.getProduct().getName()+" is out of stock");
                        response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                        return response;
                    }
                    
                    double itemPrice=0.00;
                    
                    //check for discounted item
                    if (cartItems.get(i).getDiscountId()!=null) {
                        //check if discount still valid
                        ItemDiscount discountDetails = productInventory.getItemDiscount();
                        if (discountDetails!=null) {
                            
                            double discountPrice = Utilities.Round2DecimalPoint(discountDetails.discountedPrice);
                            double dineInDiscountPrice = Utilities.Round2DecimalPoint(discountDetails.dineInDiscountedPrice);
                            double cartItemPrice = Utilities.Round2DecimalPoint(cartItems.get(i).getProductPrice().doubleValue());
                            
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "productInventory discountId:["+discountDetails.discountId+"] discountedPrice:"+discountPrice);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartItem discountId:["+cartItems.get(i).getDiscountId()+"] price:"+cartItemPrice);
                            
                            double beza = 0.00;
                            if (cart.getServiceType()!=null && cart.getServiceType()==ServiceType.DINEIN) {
                                beza = Math.abs(dineInDiscountPrice - cartItemPrice);
                            } else {
                                beza = Math.abs(discountPrice - cartItemPrice);
                            }
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartItem discountId:["+cartItems.get(i).getDiscountId()+"] beza:"+beza);
                            
                            if (discountDetails.discountId.equals(cartItems.get(i).getDiscountId()) &&
                                    beza < 0.5) {
                                //dicount still valid
                                subTotal += cartItems.get(i).getPrice() ;
                                if (cart.getServiceType()!=null && cart.getServiceType()==ServiceType.DINEIN) {
                                    itemPrice = dineInDiscountPrice;
                                } else {
                                    itemPrice = discountPrice;
                                }
                            } else {
                                //discount no more valid
                                // should return warning if prices are not same
                                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Discount not valid");
                                response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                                response.setMessage("Discount not valid");
                                return response;
                            }
                        } else {
                            //discount no more valid
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Discount not valid");
                            response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                            response.setMessage("Discount not valid");
                            return response;
                        }
                    } else { 
                        double cartPrice = Utilities.Round2DecimalPoint(cartItems.get(i).getProductPrice().doubleValue());
                        double deliverinPrice = Utilities.Round2DecimalPoint(productInventory.getPrice());
                        double dineinPrice = Utilities.Round2DecimalPoint(productInventory.getDineInPrice());
                        double bezaDeliverIn = Math.abs(cartPrice - deliverinPrice);                        
                        double bezaDineIn = Math.abs(cartPrice - dineinPrice);
                        Boolean isCustomPrice = productInventory.getProduct().getIsCustomPrice();
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Item Cart price:"+cartPrice+" deliverinPrice:"+deliverinPrice+" dineinPrice:"+dineinPrice);
                        if (isCustomPrice!=null && isCustomPrice==true) {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Dinein customPrice product, price got : cartPrice: " + cartItems.get(i).getProductPrice());
                        } else if (cart.getServiceType()==ServiceType.DELIVERIN && bezaDeliverIn>0.5) {
                            // should return warning if prices are not same
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DeliverIn  prices are not same, price got : oldPrice: " + cartItems.get(i).getProductPrice() + ", newPrice: " + String.valueOf(productInventory.getPrice()));
                            response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                            response.setMessage("Ops! The product price for "+cartItems.get(i).getProductName()+" has been updated. Please refresh the Checkout page.");                            
                            return response;
                        } else if (cart.getServiceType()==ServiceType.DINEIN && bezaDineIn>0.5) {
                            // should return warning if prices are not same
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DineIn prices are not same, price got : oldPrice: " + cartItems.get(i).getProductPrice() + ", newPrice: " + String.valueOf(productInventory.getPrice()));
                            response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                            response.setMessage("Ops! The product price for "+cartItems.get(i).getProductName()+" has been updated. Please refresh the Checkout page.");                            
                            return response;
                        }
                        subTotal += cartItems.get(i).getPrice() ;
                        itemPrice = cartItems.get(i).getProductPrice();
                    }
                    
                    //check for addOn price
                    if (cartItems.get(i).getCartItemAddOn()!=null && !cartItems.get(i).getCartItemAddOn().isEmpty()) {
                        for (int z=0;z<cartItems.get(i).getCartItemAddOn().size();z++) {
                            ProductAddOn productAddOn = cartItems.get(i).getCartItemAddOn().get(z).getProductAddOn();
                            double cartPrice = Utilities.Round2DecimalPoint(cartItems.get(i).getCartItemAddOn().get(z).getProductPrice().doubleValue());
                            double deliverinPrice = Utilities.Round2DecimalPoint(productAddOn.getPrice());
                            double dineinPrice = Utilities.Round2DecimalPoint(productAddOn.getDineInPrice());
                            double bezaDeliverIn = Math.abs(cartPrice - deliverinPrice);                        
                            double bezaDineIn = Math.abs(cartPrice - dineinPrice);   
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "AddOn Cart price:"+cartPrice+" deliverinPrice:"+deliverinPrice+" dineinPrice:"+dineinPrice);
                            if (cart.getServiceType()==ServiceType.DELIVERIN && bezaDeliverIn>0.5 ) {
                                // should return warning if prices are not same
                                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DeliverIn AddOn prices are not same, price got : cartPrice:" + cartPrice + ", deliverinPrice:" + deliverinPrice);
                                response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                                response.setMessage("Ops! The Add-On price for "+cartItems.get(i).getProductName()+" has been updated. Please refresh the Checkout page.");                            
                                return response;
                            } else if (cart.getServiceType()==ServiceType.DINEIN && bezaDineIn>0.5) {
                                // should return warning if prices are not same
                                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DineIn AddOn prices are not same, price got : cartPrice:" + cartPrice + ", dineinPrice:" + dineinPrice);
                                response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                                response.setMessage("Ops! The Add-On price for "+cartItems.get(i).getProductName()+" has been updated. Please refresh the Checkout page.");                            
                                return response;
                            }
                            subTotal += cartItems.get(i).getCartItemAddOn().get(z).getPrice() ;
                            itemPrice = itemPrice + cartItems.get(i).getCartItemAddOn().get(z).getProductPrice() ;
                        }
                    }
                    
                    //creating orderItem
                    OrderItem orderItem = new OrderItem();
                    orderItem.setItemCode(cartItems.get(i).getItemCode());
                    orderItem.setProductId(cartItems.get(i).getProductId());
                    orderItem.setProductName((productInventory.getProduct() != null) ? productInventory.getProduct().getName() : "");
                    orderItem.setProductPrice((float)itemPrice);
                    orderItem.setQuantity(cartItems.get(i).getQuantity());
                    orderItem.setSKU(productInventory.getSKU());
                    orderItem.setSpecialInstruction(cartItems.get(i).getSpecialInstruction());
                    orderItem.setWeight(cartItems.get(i).getWeight());
                    orderItem.setPrice(cartItems.get(i).getQuantity() * (float)itemPrice);
                    if (variantList!=null) {
                        orderItem.setProductVariant(variantList);
                    }
                    if (cartItems.get(i).getDiscountId()!=null) {
                        orderItem.setDiscountId(cartItems.get(i).getDiscountId());
                        orderItem.setNormalPrice(cartItems.get(i).getNormalPrice());
                        orderItem.setDiscountLabel(cartItems.get(i).getDiscountLabel());
                        orderItem.setDiscountCalculationType(cartItems.get(i).getDiscountCalculationType());
                        orderItem.setDiscountCalculationValue(cartItems.get(i).getDiscountCalculationValue());                        
                    }
                    //orderPostService.postOrderLink(order.getId(), order.getStoreId(), orderItems);


                    //add cart subitem if any
                    List<OrderSubItem> orderSubItemList=null;
                    if (cartItems.get(i).getCartSubItem()!=null) {
                        orderSubItemList = new ArrayList();
                        for (int x=0;x<cartItems.get(i).getCartSubItem().size();x++) {
                            CartSubItem cartSubItem = cartItems.get(i).getCartSubItem().get(x);

                             // check every items price in product service
                            ProductInventory subProductInventory = productService.getProductInventoryById(cart.getStoreId(), cartSubItem.getProductId(), cartSubItem.getItemCode());
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got subproductinventory against itemcode:" + cartSubItem.getItemCode());
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got subproductinventory: " + cartSubItem.getItemCode(), subProductInventory);

                            //get product variant
                            ProductInventory subProductInventoryDB = productInventoryRepository.findByItemCode(cartItems.get(i).getItemCode());       
                            String subVariantList = null;
                            if (productInventory.getProductInventoryItems().size()>0) {
                                for (int y=0;y<subProductInventoryDB.getProductInventoryItems().size();y++) {
                                    ProductInventoryItem productInventoryItem = productInventory.getProductInventoryItems().get(y);
                                    ProductVariantAvailable productVariantAvailable = productInventoryItem.getProductVariantAvailable();
                                    String variant = productVariantAvailable.getValue();
                                    if (subVariantList==null)
                                        subVariantList = variant;
                                    else
                                        subVariantList = subVariantList + "," + variant;
                                }
                            }

                            OrderSubItem orderSubItem = new OrderSubItem();
                            orderSubItem.setItemCode(cartSubItem.getItemCode());
                            orderSubItem.setProductId(cartSubItem.getProductId());
                            orderSubItem.setProductName(cartSubItem.getProductName());
                            orderSubItem.setProductName((subProductInventory.getProduct() != null) ? subProductInventory.getProduct().getName() : "");
                            if (subVariantList!=null) {
                                orderSubItem.setProductVariant(subVariantList);
                            }
                            orderSubItem.setQuantity(cartSubItem.getQuantity());
                            orderSubItem.setSpecialInstruction(cartSubItem.getSpecialInstruction());
                            orderSubItem.setSKU(subProductInventory.getSKU());
                            orderSubItem.setWeight(cartSubItem.getWeight());
                            orderSubItemList.add(orderSubItem);
                        }
                        orderItem.setOrderSubItem(orderSubItemList);
                    }
                    
                    //add addOn if any
                    List<OrderItemAddOn> orderItemAddOnList=null;
                    if (cartItems.get(i).getCartItemAddOn()!=null && !cartItems.get(i).getCartItemAddOn().isEmpty()) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Cart item addOn size:"+cartItems.get(i).getCartItemAddOn().size());
                        orderItemAddOnList = new ArrayList();
                        for (int z=0;z<cartItems.get(i).getCartItemAddOn().size();z++) {
                            OrderItemAddOn orderItemAddOn = new OrderItemAddOn();
                            orderItemAddOn.setOrderItemId(cartItems.get(i).getId());
                            orderItemAddOn.setProductAddOnId(cartItems.get(i).getCartItemAddOn().get(z).getProductAddOnId());
                            orderItemAddOn.setPrice(cartItems.get(i).getCartItemAddOn().get(z).getPrice());
                            orderItemAddOn.setProductPrice(cartItems.get(i).getCartItemAddOn().get(z).getProductPrice());
                            orderItemAddOnList.add(orderItemAddOn);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Added AddOnItem:"+orderItemAddOn.toString());
                        }  
                        orderItem.setOrderItemAddOn(orderItemAddOnList);                            
                    }
                    
                    //adding new orderItem to orderItems list
                    orderItems.add(orderItem);

                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "added orderItem to order list: " + orderItem.toString());
                }
                
                //get delivery charges from delivery-service
                String deliveryQuotationId = cod.getOrderPaymentDetails().getDeliveryQuotationReferenceId();
                if (deliveryQuotationId!=null) {
                    DeliveryQuotation deliveryQuotation = deliveryService.getDeliveryQuotation(deliveryQuotationId);
                    double deliveryCharge = deliveryQuotation.getAmount();
                    VehicleType vehicleType = deliveryQuotation.getVehicleType();
                    String fulfillmentType = deliveryQuotation.getFulfillmentType();
                    cod.getOrderPaymentDetails().setDeliveryQuotationAmount(deliveryCharge);
                    if (deliveryQuotation.getCombinedDelivery()!=null) {
                        cod.getOrderPaymentDetails().setIsCombinedDelivery(deliveryQuotation.getCombinedDelivery());                    
                    } else {
                        cod.getOrderPaymentDetails().setIsCombinedDelivery(false);                    
                    }
                    cod.getOrderShipmentDetails().setVehicleType(vehicleType.name());
                    cod.getOrderShipmentDetails().setFulfilmentType(fulfillmentType);
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DeliveryCharge from delivery-service:"+deliveryCharge+" vehicleType:"+vehicleType);
                } else {
                    cod.getOrderPaymentDetails().setDeliveryQuotationAmount(0.00);
                    cod.getOrderPaymentDetails().setIsCombinedDelivery(false);  
                }               
                
                order.setCartId(cartId);                    
                order.setCompletionStatus(OrderStatus.RECEIVED_AT_STORE);
                order.setPaymentStatus(PaymentStatus.PENDING);
                order.setCustomerId(cod.getCustomerId());
                
                if (cart.getServiceType()!=null && cart.getServiceType()==ServiceType.DINEIN) {
                    order.setDeliveryCharges(0.00);
                    if (cod.getPaymentType()!=null) {
                        order.setPaymentType(cod.getPaymentType()); 
                    } else {
                        order.setPaymentType(storeWithDetials.getDineInPaymentType()); 
                    }
                    order.setDineInOption(storeWithDetials.getDineInOption());
                    order.setDineInPack(cod.getDineInPack());                    
                    if (cod.getPaymentType()!=null) {
                        order.setPaymentType(cod.getPaymentType());
                    }
                } else {
                    order.setDeliveryCharges(cod.getOrderPaymentDetails().getDeliveryQuotationAmount());
                    order.setPaymentType(storeWithDetials.getPaymentType());                
                }
                order.setCustomerNotes(cod.getCustomerNotes());
                order.setTableNo(tableNo);
                order.setZone(zone);
                
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "serviceChargesPercentage: " + storeWithDetials.getServiceChargesPercentage());

                // setting invoice id
                String invoicePrefix = storeWithDetials.getNameAbreviation();
                if (storeWithDetials.getStorePrefix()!=null) {
                    invoicePrefix = storeWithDetials.getStorePrefix();
                } 
                String invoiceId = TxIdUtil.generateInvoiceId(storeWithDetials.getId(), invoicePrefix, storeRepository);
                order.setInvoiceId(invoiceId);

                // setting this empty
                order.setPrivateAdminNotes("");
                
                //set staff id
                order.setStaffId(staffId);
                
                //set payment channel
                order.setPaymentChannel(paymentChannel);
                
                OrderObject orderTotalObject = OrderCalculation.CalculateOrderTotal(cart, storeWithDetials.getServiceChargesPercentage(), storeCommission, 
                        cod.getOrderPaymentDetails().getDeliveryQuotationAmount(), cod.getOrderShipmentDetails().getDeliveryType(), null, customerStoreVoucher, storeWithDetials.getVerticalCode(), 
                        cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix, cartItems);                
                
                if (orderTotalObject.getGotError()) {
                    // should return warning if got error
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error while calculating discount:"+orderTotalObject.getErrorMessage());
                    response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                    response.setMessage(orderTotalObject.getErrorMessage());
                    return response;
                }
                
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order platform voucherId:"+orderTotalObject.getVoucherId());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order platform voucherDiscount:"+orderTotalObject.getVoucherDiscount());
                
                order.setSubTotal(orderTotalObject.getSubTotal());
                order.setAppliedDiscount(orderTotalObject.getAppliedDiscount());
                order.setAppliedDiscountDescription(orderTotalObject.getAppliedDiscountDescription());
                order.setDeliveryDiscount(orderTotalObject.getDeliveryDiscount());
                order.setDeliveryDiscountDescription(orderTotalObject.getDeliveryDiscountDescription());                
                order.setStoreServiceCharges(orderTotalObject.getStoreServiceCharge());
                order.setTotal(orderTotalObject.getTotal());
                order.setKlCommission(orderTotalObject.getKlCommission());
                order.setStoreShare(orderTotalObject.getStoreShare());
                order.setDiscountId(orderTotalObject.getDiscountId());
                order.setDiscountCalculationType(orderTotalObject.getDiscountCalculationType());
                order.setDiscountCalculationValue(orderTotalObject.getDiscountCalculationValue());
                order.setDiscountMaxAmount(orderTotalObject.getDiscountMaxAmount());
                order.setDeliveryDiscountMaxAmount(orderTotalObject.getDeliveryDiscountMaxAmount());
                order.setTotalReminderSent(0);
                order.setVoucherDiscount(orderTotalObject.getVoucherDiscount());
                order.setVoucherId(orderTotalObject.getVoucherId()); 
                order.setStoreVoucherDiscount(orderTotalObject.getStoreVoucherDiscount());
                order.setStoreVoucherId(orderTotalObject.getStoreVoucherId());
                order.setTotalDataObject(orderTotalObject);
                if (cart.getServiceType()!=null) {
                    order.setServiceType(cart.getServiceType());
                } else {
                    order.setServiceType(ServiceType.DELIVERIN);
                }
                if (channel!=null) {
                    order.setChannel(channel);
                } else {
                    order.setChannel(Channel.DELIVERIN);
                }    
                
                // saving order object to get order Id
                order = orderRepository.save(order);
                    
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order posted successfully orderId: " + order.getId());
                // save payment details
                cod.getOrderPaymentDetails().setOrderId(order.getId());
                order.setOrderPaymentDetail(orderPaymentDetailRepository.save(cod.getOrderPaymentDetails()));
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order payment details inserted successfully: " + order.getOrderPaymentDetail().toString());
                // save shipment detials
                Boolean storePickup = cod.getOrderShipmentDetails().getStorePickup();
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store Pickup:"+storePickup);
                
                if (cart.getServiceType()!=null && cart.getServiceType()==ServiceType.DINEIN) {
                    storePickup=true;
                }
                
                if (storePickup==null) {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Delivery Type:["+cod.getOrderShipmentDetails().getDeliveryType()+"]");
                    storePickup=false;
                    cod.getOrderShipmentDetails().setStorePickup(false);
                    if (cod.getOrderShipmentDetails().getDeliveryType()!=null) {
                        order.setDeliveryType(cod.getOrderShipmentDetails().getDeliveryType());
                    } else {
                        order.setDeliveryType(storeDeliveryDetail.getType());
                    }                    
                } else {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Delivery Type:["+cod.getOrderShipmentDetails().getDeliveryType()+"]");
                     if (storePickup) {
                        order.setDeliveryType(DeliveryType.PICKUP.name());
                     } else {
                        if (cod.getOrderShipmentDetails().getDeliveryType()!=null) {
                           order.setDeliveryType(cod.getOrderShipmentDetails().getDeliveryType());
                       } else {
                           order.setDeliveryType(storeDeliveryDetail.getType());
                       }
                     }
                }
                cod.getOrderShipmentDetails().setOrderId(order.getId());
                if (cod.getOrderShipmentDetails().getPhoneNumber()!=null && cod.getOrderShipmentDetails().getPhoneNumber().startsWith("0")) {
                    String countryCode = cart.getStore().getRegionCountry().getCountryCode();
                    String customerMsisdn = countryCode + cod.getOrderShipmentDetails().getPhoneNumber().substring(1);
                    cod.getOrderShipmentDetails().setPhoneNumber(customerMsisdn);               
                }
                order.setOrderShipmentDetail(orderShipmentDetailRepository.save(cod.getOrderShipmentDetails()));
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order shipment details inserted successfully: " + order.getOrderShipmentDetail().toString());                
                
                // saving order delivery type
                order = orderRepository.save(order);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order delivery type:"+order.getDeliveryType());
                
                OrderItem orderItem = null;
                Product product;
                ProductInventory productInventory;

                // inserting order items now
                for (int i = 0; i < orderItems.size(); i++) {
                    // insert orderItem 
                    orderItems.get(i).setOrderId(order.getId());
                    orderItem = orderItemRepository.save(orderItems.get(i));
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderItem created with id: " + orderItem.getId() + ", orderId: " + orderItem.getOrderId());

                    //add cart subitem if any
                    List<OrderSubItem> orderSubItemList=null;
                    if (orderItem.getOrderSubItem()!=null) {
                        orderSubItemList = new ArrayList();
                        for (int x=0;x<orderItem.getOrderSubItem().size();x++) {                                
                            OrderSubItem orderSubItem = orderItem.getOrderSubItem().get(x);
                            orderSubItem.setOrderItemId(orderItem.getId());
                            orderSubItemRepository.save(orderSubItem);
                        }                            
                    }
                    
                    //add cart add-on if any
                    List<OrderItemAddOn> orderAddOnItemList=null;
                    if (orderItem.getOrderItemAddOn()!=null) {
                        orderAddOnItemList = new ArrayList();
                        for (int x=0;x<orderItem.getOrderItemAddOn().size();x++) {                                
                            OrderItemAddOn orderItemAddOn = orderItem.getOrderItemAddOn().get(x);
                            orderItemAddOn.setOrderItemId(orderItem.getId());
                            orderItemAddOnRepository.save(orderItemAddOn);
                        }                            
                    }
                    
                    // getting product information if product tracking is enabled we will reduce the quantity
                    product = productService.getProductById(order.getStoreId(), orderItems.get(i).getProductId());
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Got product details of orderItem: " + product.toString());

                    if (product.isTrackQuantity()) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Product tracking is enable");
//                            productInventory = productService.reduceProductInventory(order.getStoreId(), orderItems.get(i).getProductId(), orderItems.get(i).getItemCode(), orderItems.get(i).getQuantity());

                        ProductInventory reduceQuantityProductInventory = productInventoryRepository.findByItemCode(orderItems.get(i).getItemCode());
                        int oldQuantity = reduceQuantityProductInventory.getQuantity();
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "oldQuantity: " + oldQuantity + " for itemCode:" + orderItems.get(i).getItemCode());
                        int newQuantity = oldQuantity - orderItems.get(i).getQuantity();
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "oldQuantity: " + oldQuantity + " for itemCode:" + orderItems.get(i).getItemCode());
                        reduceQuantityProductInventory.setQuantity(newQuantity);
                        productInventoryRepository.save(reduceQuantityProductInventory);
                        if (reduceQuantityProductInventory.getQuantity() <= product.getMinQuantityForAlarm()) {
                            //sending notification for product is going out of stock
                            //we can send email as well
                            orderPostService.sendMinimumQuantityAlarm(order.getId(), order.getStoreId(), orderItems.get(i), reduceQuantityProductInventory.getQuantity());
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "intimation send for out of stock product id: " + orderItems.get(i).getProductId() + ", SKU: " + orderItems.get(i).getSKU() + ", Name: " + reduceQuantityProductInventory.getProduct().getName());
                        }

                        if (!product.isAllowOutOfStockPurchases() && reduceQuantityProductInventory.getQuantity() <= 0) {
                            // making this product variant outof stock
                            productInventory = productService.changeProductStatus(order.getStoreId(), orderItems.get(i).getProductId(), orderItems.get(i).getItemCode(), ProductStatus.OUTOFSTOCK);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "this product variant is out of stock now storeId: " + order.getStoreId() + ", productId: " + orderItems.get(i).getProductId() + ", itemCode: " + orderItems.get(i).getItemCode());
                        }

                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Product tracking is not enabled by marchant");
                    }

                }
                
                /*
                if (customerPlatformVoucher!=null) {
                    voucherRepository.deductVoucherBalance(customerPlatformVoucher.getVoucherId());
                    customerPlatformVoucher.setIsUsed(true);
                    customerVoucherRepository.save(customerPlatformVoucher);
                }*/
                
                
                
                //clear cart item for COD. for online payment only clear after payment confirmed
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order Payment Type:"+order.getPaymentType());
                if (order.getPaymentType().equals(StorePaymentType.COD.name())) {
                    cartItemRepository.clearCartItem(cart.getId());
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Cart item cleared for cartId:"+cart.getId());
                    
                    if (customerStoreVoucher!=null) {
                        voucherRepository.deductVoucherBalance(customerStoreVoucher.getVoucherId());
                        customerStoreVoucher.setIsUsed(true);
                        customerVoucherRepository.save(customerStoreVoucher);
                    }                    
                    
                }
                
                //register user if not registered
                if (cod.getCustomerId()==null) {
                    //check if email already registered
                    List<Customer> existingCustomer = customerRepository.findByEmail(cod.getOrderShipmentDetails().getEmail());
                    if (existingCustomer.size()>0) {
                         //email already registered
                        String customerId = existingCustomer.get(0).getId();
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Existing customer with id: " + customerId);
                        order.setCustomerId(customerId);
                        orderRepository.save(order);
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "updated customerId: " + customerId + " to order: " + order.getId());                        
                    } else {   
                        //register new user
                        String customerId = customerService.addCustomer(cod.getOrderShipmentDetails(), order.getStoreId());

                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "customerId: " + customerId);

                        if (customerId != null) {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "customer created with id: " + customerId);
                            order.setCustomerId(customerId);
                            orderRepository.save(order);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "updated customerId: " + customerId + " to order: " + order.getId());
                        } else {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Fail to generate customerId to order: " + order.getId());
                        }                                           
                    }
                }
                    
                //get order completion config
                String verticalId = storeWithDetials.getVerticalCode();                    
                String storeDeliveryType = storeWithDetials.getStoreDeliveryDetail().getType();
                if (order.getServiceType()!=null && order.getServiceType()==ServiceType.DINEIN) {
                    storeDeliveryType = order.getDineInOption().name();
                }
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "ServiceType:"+order.getServiceType()+" Status:"+OrderStatus.RECEIVED_AT_STORE.name()+" VerticalId:"+verticalId+" storePickup:"+storePickup+" deliveryType:"+storeDeliveryType+" paymentType:"+order.getPaymentType());
                List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, OrderStatus.RECEIVED_AT_STORE.name(), storePickup, storeDeliveryType, order.getPaymentType());
                OrderCompletionStatusConfig orderCompletionStatusConfig = null;
                if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
                    Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for status: " + OrderStatus.RECEIVED_AT_STORE.name());             
                } else {        
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size()+" regionVertical:"+storeWithDetials.getRegionVertical());
                    orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);                        

                    //send email to customer if config allows
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "email to customer: " + orderCompletionStatusConfig.getEmailToCustomer());
                    if (orderCompletionStatusConfig.getEmailToCustomer()) {
                        String emailContent = orderCompletionStatusConfig.getCustomerEmailContent();
                        if (emailContent != null) {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "email content is not null");
                            //sending email
                            try {
                                RegionCountry regionCountry = null;
                                Optional<RegionCountry> t = regionCountriesRepository.findById(storeWithDetials.getRegionCountryId());
                                if (t.isPresent()) {
                                    regionCountry = t.get();
                                }
                                
                                String customerEmail=null;
                                boolean sendActivationLink=false;
                                
                                //get customer info
                                Optional<Customer> customerOpt = customerRepository.findById(order.getCustomerId());
                                Customer customer = null;                                    
                                if (customerOpt.isPresent()) {
                                    customer = customerOpt.get();
                                    if (customer.getIsActivated()==false)
                                        sendActivationLink = true;
                                    customerEmail = customer.getEmail();
                                }

                                if (sendReceiptToReceiver!=null && sendReceiptToReceiver) {
                                   //get receiver info
                                   if (order.getOrderShipmentDetail()!=null) {
                                       customerEmail = order.getOrderShipmentDetail().getEmail();
                                   } 
                                } 
                                
                                String deliveryChargesRemarks="";
                                if (order.getOrderPaymentDetail().getIsCombinedDelivery()) {
                                    List<OrderPaymentDetail> orderPaymentDetailList = orderPaymentDetailRepository.findByDeliveryQuotationReferenceId(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId());
                                    deliveryChargesRemarks = " (combined x"+orderPaymentDetailList.size()+" shops)";
                                }

                                emailContent = MessageGenerator.generateEmailContent(emailContent, order, storeWithDetials, orderItems, order.getOrderShipmentDetail(), null, regionCountry, sendActivationLink, storeWithDetials.getRegionVertical().getCustomerActivationNotice(), customerEmail, assetServiceBaseUrl, deliveryChargesRemarks, 0.00);
                                Email email = new Email();
                                ArrayList<String> tos = new ArrayList<>();
                                tos.add(order.getOrderShipmentDetail().getEmail());
                                String[] to = Utilities.convertArrayListToStringArray(tos);
                                email.setTo(to);
                                email.setFrom(storeWithDetials.getRegionVertical().getSenderEmailAdress());
                                email.setFromName(storeWithDetials.getRegionVertical().getSenderEmailName());
                                email.setDomain(storeWithDetials.getRegionVertical().getDomain());                                
                                email.setRawBody(emailContent);
                                Body body = new Body();
                                body.setCurrency(storeWithDetials.getRegionCountry().getCurrencyCode());
                                body.setDeliveryAddress(order.getOrderShipmentDetail().getAddress());
                                body.setDeliveryCity(order.getOrderShipmentDetail().getCity());
                                body.setOrderStatus(OrderStatus.RECEIVED_AT_STORE);
                                body.setDeliveryCharges(order.getOrderPaymentDetail().getDeliveryQuotationAmount());
                                body.setTotal(order.getTotal());
                                body.setInvoiceId(order.getInvoiceId());

                                body.setStoreAddress(storeWithDetials.getAddress());
                                body.setStoreContact(storeWithDetials.getPhoneNumber());
                                if (storeWithDetials.getStoreLogoUrl()!=null) {
                                    body.setLogoUrl(storeWithDetials.getStoreLogoUrl());
                                } else {
                                    body.setLogoUrl(storeWithDetials.getRegionVertical().getDefaultLogoUrl());
                                }
                                body.setStoreName(storeWithDetials.getName());
                                body.setOrderItems(orderItems);
                                email.setBody(body);
                                emailService.sendEmail(email);
                            } catch (Exception ex) {
                                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error sending email :", ex);
                            }
                        } else {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "email content is null");
                        }
                    }

                    //send rocket chat message
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "rc message to store: " + orderCompletionStatusConfig.getRcMessage());
                    if (orderCompletionStatusConfig.getRcMessage()) {
                        String rcMessageContent = orderCompletionStatusConfig.getRcMessageContent();
                        if (rcMessageContent != null) {

                            try {
                                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "rc message content is not null");
                                rcMessageContent = MessageGenerator.generateRocketChatMessageContent(rcMessageContent, order, orderItems, onboardingOrderLink);
                                //sending rc messsage

                                orderPostService.postOrderLink(rcMessageContent, order.getStoreId());
                            } catch (Exception ex) {
                                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error sending rc message :", ex);
                            }
                        } else {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "rc message content null");
                        }

                    }

                    //send push notification to DCM message
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "pushNotificationToMerchat to store: " + orderCompletionStatusConfig.getPushNotificationToMerchat());
                    if (orderCompletionStatusConfig.getPushNotificationToMerchat()) {
                        String pushNotificationTitle = orderCompletionStatusConfig.getStorePushNotificationTitle();
                        String pushNotificationContent = orderCompletionStatusConfig.getStorePushNotificationContent();
                        try {
                            fcmService.sendPushNotification(order, storeWithDetials.getId(), storeWithDetials.getName(), pushNotificationTitle, pushNotificationContent, OrderStatus.RECEIVED_AT_STORE, storeWithDetials.getRegionVertical().getDomain());
                        } catch (Exception e) {
                            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "pushNotificationToMerchat error ", e);
                        }

                    }
                    
                    //send push notification to WA alert to admin
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getPushWAToAdmin to store: " + orderCompletionStatusConfig.getPushWAToAdmin());
                    if (orderCompletionStatusConfig.getPushWAToAdmin()) {
                        try {
                            //String storeName, String invoiceNo, String orderId, String merchantToken
                            whatsappService.sendAdminAlert(OrderStatus.RECEIVED_AT_STORE.name(), storeWithDetials.getName(), order.getInvoiceId(), order.getId(), DateTimeUtil.currentTimestamp());
                        } catch (Exception e) {
                            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "pushNotificationToMerchat error ", e);
                        }

                    }
                
                    //send push notification to WA alert to customer
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getPushWAToCustomer to store: " + orderCompletionStatusConfig.getPushWAToCustomer());
                    if (orderCompletionStatusConfig.getPushWAToCustomer()) {
                        try {
                            //get customer info
                            Optional<Customer> customerOpt = customerRepository.findById(order.getCustomerId());
                            String customerMsisdn = null;
                            boolean isRegisteredUser = false;
                            if (customerOpt.isPresent()) {
                                Customer customer = customerOpt.get();
                                customerMsisdn = customer.getPhoneNumber();
                                if (customer.getIsActivated()) {
                                    isRegisteredUser=true;
                                }
                            }
                            
                            if (sendReceiptToReceiver!=null && sendReceiptToReceiver) {
                                //get receiver info
                                if (order.getOrderShipmentDetail()!=null) {
                                    customerMsisdn = order.getOrderShipmentDetail().getPhoneNumber();
                                } 
                            }
                            
                            //String storeName, String invoiceNo, String orderId, String merchantToken
                            String invoiceUrl = invoiceBaseUrl + "/" + order.getId();
                            whatsappService.sendCustomerAlert(customerMsisdn, OrderStatus.RECEIVED_AT_STORE.name(), storeWithDetials.getName(), order.getInvoiceId(), order.getId(), DateTimeUtil.currentTimestamp(), orderCompletionStatusConfig.getPushWAToCustomerTemplateName(), orderCompletionStatusConfig.getPushWAToCustomerTemplateFormat(), storeWithDetials.getCity(), invoiceUrl, isRegisteredUser, order.getServiceType());
                        } catch (Exception e) {
                            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "pushNotificationToMerchat error ", e);
                        }

                    }
                }
                
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Everything is fine thanks for using this API for placing order");
                response.setStatus(HttpStatus.CREATED.value());
                response.setData(order);
                return response;
               
            } catch (Exception ex) {
                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "exception occur while creating order ", ex);
                response.setMessage(ex.getMessage());
                response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                return response;
            }
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving order", exp);
            response.setMessage(exp.getMessage());
            response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
            return response;
        }
        
    }

    //TODO
    //create coupon creation  response
    public static HttpResponse placeCoupon(
            String requestUri, Cart cart,
            List<CartItem> cartItems, COD cod,
            StoreWithDetails storeWithDetials,
            CustomerVoucher customerStoreVoucher,
            String groupOrderId,
            Channel channel,
            String logprefix,
            CartItemRepository cartItemRepository,
            ProductInventoryRepository productInventoryRepository,
            StoreDiscountRepository storeDiscountRepository,
            StoreDiscountTierRepository storeDiscountTierRepository,
            OrderRepository orderRepository,
            OrderPaymentDetailRepository orderPaymentDetailRepository,
            StoreRepository storeRepository,
            CustomerRepository customerRepository,
            ProductService productService,
            CustomerService customerService
            ){

        HttpResponse response = new HttpResponse(requestUri);
        // create order object
        Order order = new Order();
        String cartId = cart.getId();

        try{
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart exists against cartId: " + cartId);

            StoreCommission storeCommission = productService.getStoreCommissionByStoreId(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store commission: " + storeCommission);

            Double subTotal = 0.0;
            List<OrderItem> orderItems = new ArrayList<OrderItem>();
            try {
                // check store payment type
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix,
                        "Store with payment type: " + storeWithDetials.getPaymentType());
                order.setStoreId(storeWithDetials.getId());

                for (int i = 0; i < cartItems.size(); i++) {
                    // check every items price in product service
                    ProductInventory productInventory = productService.getProductInventoryById(
                            cart.getStoreId(), cartItems.get(i).getProductId(),
                            cartItems.get(i).getItemCode());
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got productinventory against itemcode:" + cartItems.get(i).getItemCode());
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got productinventory: " + cartItems.get(i).getItemCode(), productInventory);

                    //get product variant
                    ProductInventory productInventoryDB = productInventoryRepository.findByItemCode(cartItems.get(i).getItemCode());
                    String variantList = null;
                    if (!productInventory.getProductInventoryItems().isEmpty()) {
                        for (int x=0;x<productInventoryDB.getProductInventoryItems().size();x++) {
                            ProductInventoryItem productInventoryItem = productInventory.getProductInventoryItems().get(x);
                            ProductVariantAvailable productVariantAvailable = productInventoryItem.getProductVariantAvailable();
                            String variant = productVariantAvailable.getValue();
                            if (variantList==null)
                                variantList = variant;
                            else
                                variantList = variantList + "," + variant;
                        }
                    }

                    //check for stock
                    if (productInventory.getQuantity()<cartItems.get(i).getQuantity()
                            && !productInventory.getProduct().isAllowOutOfStockPurchases()) {
                        //out of stock
                        response.setMessage(productInventory.getProduct().getName()+" is out of stock");
                        response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                        return response;
                    }

                    double itemPrice=0.00;

                    //check for discounted item
                    if (cartItems.get(i).getDiscountId()!=null) {
                        //check if discount still valid
                        ItemDiscount discountDetails = productInventory.getItemDiscount();
                        if (discountDetails!=null) {

                            double discountPrice = Utilities.Round2DecimalPoint(discountDetails.discountedPrice);
                            double dineInDiscountPrice = Utilities.Round2DecimalPoint(discountDetails.dineInDiscountedPrice);
                            double cartItemPrice = Utilities.Round2DecimalPoint(cartItems.get(i).getProductPrice().doubleValue());

                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "productInventory discountId:["+discountDetails.discountId+"] discountedPrice:"+discountPrice);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartItem discountId:["+cartItems.get(i).getDiscountId()+"] price:"+cartItemPrice);

                            double beza = 0.00;
                            if (cart.getServiceType()!=null && cart.getServiceType()==ServiceType.DINEIN) {
                                beza = Math.abs(dineInDiscountPrice - cartItemPrice);
                            } else {
                                beza = Math.abs(discountPrice - cartItemPrice);
                            }
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartItem discountId:["+cartItems.get(i).getDiscountId()+"] beza:"+beza);

                            if (discountDetails.discountId.equals(cartItems.get(i).getDiscountId()) &&
                                    beza < 0.5) {
                                //dicount still valid
                                subTotal += cartItems.get(i).getPrice() ;
                                if (cart.getServiceType()!=null && cart.getServiceType()==ServiceType.DINEIN) {
                                    itemPrice = dineInDiscountPrice;
                                } else {
                                    itemPrice = discountPrice;
                                }
                            } else {
                                //discount no more valid
                                // should return warning if prices are not same
                                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Discount not valid");
                                response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                                response.setMessage("Discount not valid");
                                return response;
                            }
                        } else {
                            //discount no more valid
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Discount not valid");
                            response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                            response.setMessage("Discount not valid");
                            return response;
                        }
                    } else {
                        double cartPrice = Utilities.Round2DecimalPoint(cartItems.get(i).getProductPrice().doubleValue());
                        double deliverinPrice = Utilities.Round2DecimalPoint(0.0);
                        double dineinPrice = Utilities.Round2DecimalPoint(productInventory.getDineInPrice());
                        double bezaDeliverIn = Math.abs(cartPrice - deliverinPrice);
                        double bezaDineIn = Math.abs(cartPrice - dineinPrice);
                        Boolean isCustomPrice = productInventory.getProduct().getIsCustomPrice();
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Item Cart price:"+cartPrice+" deliverinPrice:"+deliverinPrice+" dineinPrice:"+dineinPrice);
                        if (isCustomPrice!=null && isCustomPrice==true) {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Dinein customPrice product, price got : cartPrice: " + cartItems.get(i).getProductPrice());
                        } else if (cart.getServiceType()==ServiceType.DELIVERIN && bezaDeliverIn>0.5) {
                            // should return warning if prices are not same
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DeliverIn  prices are not same, price got : oldPrice: " + cartItems.get(i).getProductPrice() + ", newPrice: " + String.valueOf(productInventory.getPrice()));
                            response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                            response.setMessage("Ops! The product price for "+cartItems.get(i).getProductName()+" has been updated. Please refresh the Checkout page.");
                            return response;
                        } else if (cart.getServiceType()==ServiceType.DINEIN && bezaDineIn>0.5) {
                            // should return warning if prices are not same
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DineIn prices are not same, price got : oldPrice: " + cartItems.get(i).getProductPrice() + ", newPrice: " + String.valueOf(productInventory.getPrice()));
                            response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                            response.setMessage("Ops! The product price for "+cartItems.get(i).getProductName()+" has been updated. Please refresh the Checkout page.");
                            return response;
                        }
                        subTotal += cartItems.get(i).getPrice() ;
                        itemPrice = cartItems.get(i).getProductPrice();
                    }

                    //check for addOn price
                    if (cartItems.get(i).getCartItemAddOn()!=null && !cartItems.get(i).getCartItemAddOn().isEmpty()) {
                        for (int z=0;z<cartItems.get(i).getCartItemAddOn().size();z++) {
                            ProductAddOn productAddOn = cartItems.get(i).getCartItemAddOn().get(z).getProductAddOn();
                            double cartPrice = Utilities.Round2DecimalPoint(cartItems.get(i).getCartItemAddOn().get(z).getProductPrice().doubleValue());
                            double deliverinPrice = Utilities.Round2DecimalPoint(0.0);
                            double dineinPrice = Utilities.Round2DecimalPoint(productAddOn.getDineInPrice());
                            double bezaDeliverIn = Math.abs(cartPrice - deliverinPrice);
                            double bezaDineIn = Math.abs(cartPrice - dineinPrice);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "AddOn Cart price:"+cartPrice+" deliverinPrice:"+deliverinPrice+" dineinPrice:"+dineinPrice);
                            if (cart.getServiceType()==ServiceType.DELIVERIN && bezaDeliverIn>0.5 ) {
                                // should return warning if prices are not same
                                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DeliverIn AddOn prices are not same, price got : cartPrice:" + cartPrice + ", deliverinPrice:" + deliverinPrice);
                                response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                                response.setMessage("Ops! The Add-On price for "+cartItems.get(i).getProductName()+" has been updated. Please refresh the Checkout page.");
                                return response;
                            } else if (cart.getServiceType()==ServiceType.DINEIN && bezaDineIn>0.5) {
                                // should return warning if prices are not same
                                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DineIn AddOn prices are not same, price got : cartPrice:" + cartPrice + ", dineinPrice:" + dineinPrice);
                                response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                                response.setMessage("Ops! The Add-On price for "+cartItems.get(i).getProductName()+" has been updated. Please refresh the Checkout page.");
                                return response;
                            }
                            subTotal += cartItems.get(i).getCartItemAddOn().get(z).getPrice() ;
                            itemPrice = itemPrice + cartItems.get(i).getCartItemAddOn().get(z).getProductPrice() ;
                        }
                    }

                    //creating orderItem
                    OrderItem orderItem = new OrderItem();
                    orderItem.setItemCode(cartItems.get(i).getItemCode());
                    orderItem.setProductId(cartItems.get(i).getProductId());
                    orderItem.setProductName((productInventory.getProduct() != null) ? productInventory.getProduct().getName() : "");
                    orderItem.setProductPrice((float)itemPrice);
                    orderItem.setQuantity(cartItems.get(i).getQuantity());
                    orderItem.setSKU(productInventory.getSKU());
                    orderItem.setSpecialInstruction(cartItems.get(i).getSpecialInstruction());
                    orderItem.setWeight(cartItems.get(i).getWeight());
                    orderItem.setPrice(cartItems.get(i).getQuantity() * (float)itemPrice);
                    if (variantList!=null) {
                        orderItem.setProductVariant(variantList);
                    }
                    if (cartItems.get(i).getDiscountId()!=null) {
                        orderItem.setDiscountId(cartItems.get(i).getDiscountId());
                        orderItem.setNormalPrice(cartItems.get(i).getNormalPrice());
                        orderItem.setDiscountLabel(cartItems.get(i).getDiscountLabel());
                        orderItem.setDiscountCalculationType(cartItems.get(i).getDiscountCalculationType());
                        orderItem.setDiscountCalculationValue(cartItems.get(i).getDiscountCalculationValue());
                    }
                    //orderPostService.postOrderLink(order.getId(), order.getStoreId(), orderItems);


                    //add cart sub-item if any
                    List<OrderSubItem> orderSubItemList=null;
                    if (cartItems.get(i).getCartSubItem()!=null) {
                        orderSubItemList = new ArrayList();
                        for (int x=0;x<cartItems.get(i).getCartSubItem().size();x++) {
                            CartSubItem cartSubItem = cartItems.get(i).getCartSubItem().get(x);

                            // check every items price in product service
                            ProductInventory subProductInventory = productService.getProductInventoryById(cart.getStoreId(), cartSubItem.getProductId(), cartSubItem.getItemCode());
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got subproductinventory against itemcode:" + cartSubItem.getItemCode());
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got subproductinventory: " + cartSubItem.getItemCode(), subProductInventory);

                            //get product variant
                            ProductInventory subProductInventoryDB = productInventoryRepository.findByItemCode(cartItems.get(i).getItemCode());
                            String subVariantList = null;
                            if (!productInventory.getProductInventoryItems().isEmpty()) {
                                for (int y=0;y<subProductInventoryDB.getProductInventoryItems().size();y++) {
                                    ProductInventoryItem productInventoryItem = productInventory.getProductInventoryItems().get(y);
                                    ProductVariantAvailable productVariantAvailable = productInventoryItem.getProductVariantAvailable();
                                    String variant = productVariantAvailable.getValue();
                                    if (subVariantList==null)
                                        subVariantList = variant;
                                    else
                                        subVariantList = subVariantList + "," + variant;
                                }
                            }

                            OrderSubItem orderSubItem = new OrderSubItem();
                            orderSubItem.setItemCode(cartSubItem.getItemCode());
                            orderSubItem.setProductId(cartSubItem.getProductId());
                            orderSubItem.setProductName(cartSubItem.getProductName());
                            orderSubItem.setProductName((subProductInventory.getProduct() != null) ? subProductInventory.getProduct().getName() : "");
                            if (subVariantList!=null) {
                                orderSubItem.setProductVariant(subVariantList);
                            }
                            orderSubItem.setQuantity(cartSubItem.getQuantity());
                            orderSubItem.setSpecialInstruction(cartSubItem.getSpecialInstruction());
                            orderSubItem.setSKU(subProductInventory.getSKU());
                            orderSubItem.setWeight(cartSubItem.getWeight());
                            orderSubItemList.add(orderSubItem);
                        }
                        orderItem.setOrderSubItem(orderSubItemList);
                    }

                    //add addOn if any
                    List<OrderItemAddOn> orderItemAddOnList=null;
                    if (cartItems.get(i).getCartItemAddOn()!=null && !cartItems.get(i).getCartItemAddOn().isEmpty()) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Cart item addOn size:"+cartItems.get(i).getCartItemAddOn().size());
                        orderItemAddOnList = new ArrayList();
                        for (int z=0;z<cartItems.get(i).getCartItemAddOn().size();z++) {
                            OrderItemAddOn orderItemAddOn = new OrderItemAddOn();
                            orderItemAddOn.setOrderItemId(cartItems.get(i).getId());
                            orderItemAddOn.setProductAddOnId(cartItems.get(i).getCartItemAddOn().get(z).getProductAddOnId());
                            orderItemAddOn.setPrice(cartItems.get(i).getCartItemAddOn().get(z).getPrice());
                            orderItemAddOn.setProductPrice(cartItems.get(i).getCartItemAddOn().get(z).getProductPrice());
                            orderItemAddOnList.add(orderItemAddOn);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Added AddOnItem:"+orderItemAddOn.toString());
                        }
                        orderItem.setOrderItemAddOn(orderItemAddOnList);
                    }

                    //adding new orderItem to orderItems list
                    orderItems.add(orderItem);

                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "added orderItem to order list: " + orderItem.toString());
                }

                // setting order object
                order.setCartId(cartId);
                order.setCompletionStatus(OrderStatus.RECEIVED_AT_STORE);
                order.setPaymentStatus(PaymentStatus.PENDING);
                order.setCustomerId(cod.getCustomerId());

                if (cart.getServiceType()!=null && cart.getServiceType()==ServiceType.DINEIN) {
                    order.setDeliveryCharges(0.00);
                    if (cod.getPaymentType()!=null) {
                        order.setPaymentType(cod.getPaymentType());
                    } else {
                        order.setPaymentType(storeWithDetials.getDineInPaymentType());
                    }
                    order.setDineInOption(storeWithDetials.getDineInOption());
                    order.setDineInPack(cod.getDineInPack());
                    if (cod.getPaymentType()!=null) {
                        order.setPaymentType(cod.getPaymentType());
                    }
                } else {
                    order.setDeliveryCharges(cod.getOrderPaymentDetails().getDeliveryQuotationAmount());
                    order.setPaymentType(storeWithDetials.getPaymentType());
                }


                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "serviceChargesPercentage: " + storeWithDetials.getServiceChargesPercentage());

                // setting invoice id
                String invoicePrefix = storeWithDetials.getNameAbreviation();
                if (storeWithDetials.getStorePrefix()!=null) {
                    invoicePrefix = storeWithDetials.getStorePrefix();
                }
                String invoiceId = TxIdUtil.generateInvoiceId(storeWithDetials.getId(), invoicePrefix, storeRepository);
                order.setInvoiceId(invoiceId);


                OrderObject orderTotalObject = OrderCalculation.CalculateOrderTotal(cart, storeWithDetials.getServiceChargesPercentage(), storeCommission,
                        cod.getOrderPaymentDetails().getDeliveryQuotationAmount(), cod.getOrderShipmentDetails().getDeliveryType(), null, customerStoreVoucher, storeWithDetials.getVerticalCode(),
                        cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix, cartItems);

                if (orderTotalObject.getGotError()) {
                    // should return warning if got error
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error while calculating discount:"+orderTotalObject.getErrorMessage());
                    response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                    response.setMessage(orderTotalObject.getErrorMessage());
                    return response;
                }

                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order platform voucherId:"+orderTotalObject.getVoucherId());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order platform voucherDiscount:"+orderTotalObject.getVoucherDiscount());

                order.setSubTotal(orderTotalObject.getSubTotal());
                order.setAppliedDiscount(orderTotalObject.getAppliedDiscount());
                order.setAppliedDiscountDescription(orderTotalObject.getAppliedDiscountDescription());
                order.setCustomerNotes(cod.getCustomerNotes());
                order.setStoreServiceCharges(orderTotalObject.getStoreServiceCharge());
                order.setTotal(orderTotalObject.getTotal());
                order.setKlCommission(orderTotalObject.getKlCommission());
                order.setStoreShare(orderTotalObject.getStoreShare());
                order.setDiscountId(orderTotalObject.getDiscountId());
                order.setDiscountCalculationType(orderTotalObject.getDiscountCalculationType());
                order.setDiscountCalculationValue(orderTotalObject.getDiscountCalculationValue());
                order.setDiscountMaxAmount(orderTotalObject.getDiscountMaxAmount());
                order.setDeliveryDiscountMaxAmount(orderTotalObject.getDeliveryDiscountMaxAmount());

                order.setVoucherDiscount(orderTotalObject.getVoucherDiscount());
                order.setVoucherId(orderTotalObject.getVoucherId());
                order.setStoreVoucherDiscount(orderTotalObject.getStoreVoucherDiscount());
                order.setStoreVoucherId(orderTotalObject.getStoreVoucherId());
                order.setTotalDataObject(orderTotalObject);


                // Not required for Coupon
                order.setStaffId("NOT APPICABLE");
                order.setTableNo("NOT APPLICABLE");
                order.setZone("NOT APPLICABLE");
                order.setPrivateAdminNotes("");
                order.setTotalReminderSent(0);
                order.setDeliveryDiscount(0.0);
                order.setDeliveryDiscountDescription("NOT APPLICABLE");

                if (cart.getServiceType()!=null) {
                    order.setServiceType(cart.getServiceType());
                } else {
                    order.setServiceType(ServiceType.DELIVERIN);
                }
                if (channel!=null) {
                    order.setChannel(channel);
                } else {
                    order.setChannel(Channel.DELIVERIN);
                }

                // saving order object to get order Id
                order = orderRepository.save(order);

                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order posted successfully orderId: " + order.getId());
                // save payment details
                cod.getOrderPaymentDetails().setOrderId(order.getId());
                order.setOrderPaymentDetail(orderPaymentDetailRepository.save(cod.getOrderPaymentDetails()));
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order payment details inserted successfully: " + order.getOrderPaymentDetail().toString());

                //register user if not registered
                if (cod.getCustomerId()==null) {
                    //check if email already registered
                    List<Customer> existingCustomer = customerRepository.findByEmail(cod.getOrderShipmentDetails().getEmail());
                    if (!existingCustomer.isEmpty()) {
                        //email already registered
                        String customerId = existingCustomer.get(0).getId();
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Existing customer with id: " + customerId);
                        order.setCustomerId(customerId);
                        orderRepository.save(order);
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "updated customerId: " + customerId + " to order: " + order.getId());
                    } else {
                        //register new user
                        String customerId = customerService.addCustomer(cod.getOrderShipmentDetails(), order.getStoreId());

                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "customerId: " + customerId);

                        if (customerId != null) {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "customer created with id: " + customerId);
                            order.setCustomerId(customerId);
                            orderRepository.save(order);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "updated customerId: " + customerId + " to order: " + order.getId());
                        } else {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Fail to generate customerId to order: " + order.getId());
                        }
                    }
                }


                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Everything is fine thanks for using this API for placing order");
                response.setStatus(HttpStatus.CREATED.value());
                response.setData(order);
                return response;

            } catch (Exception ex) {
                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "exception occur while creating order ", ex);
                response.setMessage(ex.getMessage());
                response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                return response;
            }
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving order", exp);
            response.setMessage(exp.getMessage());
            response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
            return response;
        }
    }
    
    
    public static boolean ProcessOrder(String orderId, String action, String logprefix, String processOrderUrl) {
        
        OrderCompletionStatusUpdate request = new OrderCompletionStatusUpdate();
        request.setOrderId(orderId);
        
        if (action.equals("CANCEL")) {
            request.setStatus(OrderStatus.CANCELED_BY_MERCHANT);
        } else if (action.equals("PROCESS")) {
            request.setStatus(OrderStatus.BEING_PREPARED);
        } else if (action.equals("PICKUP")) {
            request.setStatus(OrderStatus.AWAITING_PICKUP);
        }
        
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");
         
        HttpEntity<OrderCompletionStatusUpdate> httpEntity = new HttpEntity<>(request, headers);
        
        try {
            String url = processOrderUrl.replaceAll("%orderId%", orderId);
        
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + processOrderUrl, "");
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");
        
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);

            if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
                return true;
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not ProcessOrder res: " + res, "");
                return false;
            }
        
        } catch (Exception ex) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not ProcessOrder res: " + ex.getMessage(), "");
            return false;
        }
    }
}
