/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.enums.RefundStatus;
import com.kalsym.order.service.enums.RefundType;
import com.kalsym.order.service.enums.StorePaymentType;
import com.kalsym.order.service.model.Body;
import com.kalsym.order.service.model.Customer;
import com.kalsym.order.service.model.CustomerVoucher;
import com.kalsym.order.service.model.DeliveryOrder;
import com.kalsym.order.service.model.DeliveryResponse;
import com.kalsym.order.service.model.Email;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderGroup;
import com.kalsym.order.service.model.OrderCompletionStatusConfig;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderPaymentStatusUpdate;
import com.kalsym.order.service.model.OrderRefund;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.OrderPaymentDetail;
import com.kalsym.order.service.model.PaymentOrder;
import com.kalsym.order.service.model.Product;
import com.kalsym.order.service.model.ProductInventory;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;

import com.kalsym.order.service.utility.DateTimeUtil;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.MessageGenerator;
import com.kalsym.order.service.utility.Utilities;
import com.kalsym.order.service.model.repository.*;
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.service.FCMService;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.service.EmailService;
import com.kalsym.order.service.service.OrderPostService;
import com.kalsym.order.service.service.WhatsappService;
import com.kalsym.order.service.model.object.OrderProcessResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

/**
 *
 * @author taufik
 */
public class OrderProcessWorker {
    
    private final String logprefix;
    private final String orderId;
    private final String financeEmailAddress;
    private final String financeEmailSenderName;
    private OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate;
    private String onboardingOrderLink;
    private String invoiceBaseUrl;
    
    private OrderRepository orderRepository;
    private OrderGroupRepository orderGroupRepository;
    private StoreDetailsRepository storeDetailsRepository;
    private OrderItemRepository orderItemRepository;
    private OrderCompletionStatusConfigRepository orderCompletionStatusConfigRepository;
    private CartItemRepository cartItemRepository;
    private ProductInventoryRepository productInventoryRepository;
    private PaymentOrderRepository paymentOrderRepository;
    private OrderRefundRepository orderRefundRepository;
    private OrderShipmentDetailRepository orderShipmentDetailRepository;
    private OrderPaymentDetailRepository orderPaymentDetailRepository;
    private RegionCountriesRepository regionCountriesRepository;
    private OrderPaymentStatusUpdateRepository orderPaymentStatusUpdateRepository;
    private OrderCompletionStatusUpdateRepository orderCompletionStatusUpdateRepository;
    private CustomerRepository customerRepository;
    private VoucherRepository voucherRepository;
    private CustomerVoucherRepository customerVoucherRepository;
    
    private ProductService productService;
    private EmailService emailService;
    private WhatsappService whatsappService;
    private FCMService fcmService;
    private DeliveryService deliveryService;
    private OrderPostService orderPostService;    
    
    private boolean proceedRequestDelivery;
    private String assetServiceBaseUrl;
    
    public OrderProcessWorker(
            String logprefix, 
            String orderId, 
            String financeEmailAddress,
            String financeEmailSenderName,
            OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate,
            String onboardingOrderLink,
            String invoiceBaseUrl,
            
            OrderRepository orderRepository,
            OrderGroupRepository orderGroupRepository,
            StoreDetailsRepository storeDetailsRepository,
            OrderItemRepository orderItemRepository,
            OrderCompletionStatusConfigRepository orderCompletionStatusConfigRepository,
            CartItemRepository cartItemRepository,
            ProductInventoryRepository productInventoryRepository,
            PaymentOrderRepository paymentOrderRepository,
            OrderRefundRepository orderRefundRepository,
            OrderShipmentDetailRepository orderShipmentDetailRepository,
            OrderPaymentDetailRepository orderPaymentDetailRepository,
            RegionCountriesRepository regionCountriesRepository,
            OrderPaymentStatusUpdateRepository orderPaymentStatusUpdateRepository,
            OrderCompletionStatusUpdateRepository orderCompletionStatusUpdateRepository,
            CustomerRepository customerRepository,
            VoucherRepository voucherRepository,
            CustomerVoucherRepository customerVoucherRepository,
            
            ProductService productService,
            EmailService emailService,
            WhatsappService whatsappService,
            FCMService fcmService,
            DeliveryService deliveryService,
            OrderPostService orderPostService,
            boolean proceedRequestDelivery,
            String assetServiceBaseUrl
            ) {
        
        this.logprefix = logprefix;
        this.orderId = orderId;
        this.financeEmailAddress = financeEmailAddress;
        this.financeEmailSenderName = financeEmailSenderName;
        this.bodyOrderCompletionStatusUpdate = bodyOrderCompletionStatusUpdate;
        this.onboardingOrderLink = onboardingOrderLink;
        this.invoiceBaseUrl = invoiceBaseUrl;
        
        this.orderRepository = orderRepository;
        this.orderGroupRepository = orderGroupRepository;
        this.storeDetailsRepository = storeDetailsRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderCompletionStatusConfigRepository = orderCompletionStatusConfigRepository;
        this.cartItemRepository = cartItemRepository;
        this.productInventoryRepository = productInventoryRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.orderRefundRepository = orderRefundRepository;
        this.orderShipmentDetailRepository = orderShipmentDetailRepository;
        this.orderPaymentDetailRepository = orderPaymentDetailRepository;
        this.regionCountriesRepository = regionCountriesRepository;
        this.orderPaymentStatusUpdateRepository = orderPaymentStatusUpdateRepository;
        this.orderCompletionStatusUpdateRepository = orderCompletionStatusUpdateRepository;
        this.customerRepository = customerRepository;
        this.voucherRepository = voucherRepository;
        this.customerVoucherRepository = customerVoucherRepository;
        
        this.productService = productService;
        this.emailService = emailService;
        this.whatsappService = whatsappService;
        this.fcmService = fcmService;
        this.deliveryService = deliveryService;
        this.orderPostService = orderPostService;
        this.proceedRequestDelivery = proceedRequestDelivery;
        this.assetServiceBaseUrl = assetServiceBaseUrl;
    }
    
    public OrderProcessResult startProcessOrder() {
        OrderProcessResult orderProcessResult = new OrderProcessResult();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-completion-status-updates-confirm-put-by-order-id, orderId: " + orderId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderCompletionStatusUpdate.toString(), "");
        
        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: " + orderId);
            orderProcessResult.httpStatus = HttpStatus.NOT_FOUND;
            orderProcessResult.errorMsg = "Order not found";
            return orderProcessResult;
        }
        Order order = optOrder.get();
        Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(order.getStoreId());
        if (!optStore.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store not found with storeId: " + order.getStoreId());
            orderProcessResult.httpStatus = HttpStatus.NOT_FOUND;
            orderProcessResult.errorMsg = "Store not found";
            return orderProcessResult;
        }
        
        String newStatus = bodyOrderCompletionStatusUpdate.getStatus().toString();
        
        if (!newStatus.contains("FAILED_FIND_DRIVER") && !newStatus.contains("ASSIGNING_DRIVER")) {
            //only check if not callback from delivery-service
            if (order.getBeingProcess()!=null) {
                if (order.getBeingProcess()) {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order is being processed. orderId: " + orderId);
                    orderProcessResult.httpStatus = HttpStatus.CONFLICT;
                    orderProcessResult.errorMsg = "Order is being processed";
                    return orderProcessResult;
                }
            }
        }
        
        //check for combined delivery, cannot update to being_delivered if other order not awaiting_pickup / being_delivered
        if (newStatus.contains("BEING_DELIVERED") && order.getOrderPaymentDetail()!=null && order.getOrderPaymentDetail().getIsCombinedDelivery()) {
            List<OrderPaymentDetail> orderPaymentDetailList = orderPaymentDetailRepository.findByDeliveryQuotationReferenceId(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId());
            for (int z=0;z<orderPaymentDetailList.size();z++) {
                String relatedOrderId = orderPaymentDetailList.get(z).getOrderId();
                //check order status, if cancel no need to update tracking url
                Optional<Order> optOrderRelated = orderRepository.findById(relatedOrderId);
                if (optOrderRelated.isPresent() && optOrderRelated.get().getCompletionStatus()!=OrderStatus.AWAITING_PICKUP &&  optOrderRelated.get().getCompletionStatus()!=OrderStatus.BEING_DELIVERED) {
                    //reject update. cannot update 
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order is being processed. orderId: " + orderId);
                    orderProcessResult.httpStatus = HttpStatus.CONFLICT;
                    orderProcessResult.errorMsg = "Order is being processed";
                    return orderProcessResult;
                }
            }
        }
        
        StoreWithDetails storeWithDetails = optStore.get();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "OrderId:"+order.getId()+" invoiceNo:"+order.getInvoiceId()+" Store details got : " + storeWithDetails.toString());
        OrderStatus status = bodyOrderCompletionStatusUpdate.getStatus();
            
        String subject = null;
        String content = null;
        //String[] url = deliveryResponse.data.trackingUrl;
        String receiver = order.getOrderShipmentDetail().getEmail();
        OrderPaymentStatusUpdate orderPaymentStatusUpdate = new OrderPaymentStatusUpdate();
        Body body = new Body();

        body.setCurrency(storeWithDetails.getRegionCountry().getCurrencyCode());
        body.setDeliveryAddress(order.getOrderShipmentDetail().getAddress());
        body.setDeliveryCity(order.getOrderShipmentDetail().getCity());
        body.setOrderStatus(status);
        body.setDeliveryCharges(order.getOrderPaymentDetail().getDeliveryQuotationAmount());
        body.setTotal(order.getTotal());
        body.setInvoiceId(order.getInvoiceId());

        body.setStoreAddress(storeWithDetails.getAddress());
        body.setStoreContact(storeWithDetails.getPhoneNumber());
        if (storeWithDetails.getStoreLogoUrl()!=null) {
            body.setLogoUrl(storeWithDetails.getStoreLogoUrl());
        } else {
            body.setLogoUrl(storeWithDetails.getRegionVertical().getDefaultLogoUrl());
        }
        body.setStoreName(storeWithDetails.getName());

        //get order items
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        body.setOrderItems(orderItems);

        Email email = new Email();
        email.setBody(body);
        ArrayList<String> tos = new ArrayList<>();
        tos.add(order.getOrderShipmentDetail().getEmail());
        String[] to = Utilities.convertArrayListToStringArray(tos);
        email.setTo(to);
        email.setFrom(storeWithDetails.getRegionVertical().getSenderEmailAdress());
        email.setFromName(storeWithDetails.getRegionVertical().getSenderEmailName());
        email.setDomain(storeWithDetails.getRegionVertical().getDomain()); 
        
        String verticalId = storeWithDetails.getVerticalCode();
        Boolean storePickup = order.getOrderShipmentDetail().getStorePickup();
        String storeDeliveryType = storeWithDetails.getStoreDeliveryDetail().getType();
        newStatus = newStatus.replace(" ", "_");
        OrderStatus previousStatus = order.getCompletionStatus();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "prevStatus:"+previousStatus+" newStatus:"+newStatus+" CompletionCriteria = [verticalId:"+verticalId+" storePickup:"+storePickup+" storeDeliveryType: " + storeDeliveryType+" orderPaymentType:"+order.getPaymentType()+"]");        
        
        OrderCompletionStatusConfig orderCompletionStatusConfig = null;
                
        if (newStatus.contains("CANCELED_BY_MERCHANT")) {
            //cancel order
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Merchant Cancel Order. Read config from db");
            List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndPaymentType(verticalId, newStatus, order.getPaymentType());            
            if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for status: " + newStatus);                
            } else {        
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size());
                orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);
            }                        
        } else if (newStatus.contains("FAILED_FIND_DRIVER")) {
            //delivery-order inform cannot find rider            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "FAILED_FIND_DRIVER");
            List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStoreDeliveryType(verticalId, newStatus, storeDeliveryType);            
            if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for status: " + newStatus);                
            } else {        
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size());
                orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);
            }
        } else if (newStatus.contains("ASSIGNING_DRIVER")) {
            //delivery-order inform assigning driver            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "ASSIGNING_DRIVER");
            status = OrderStatus.AWAITING_PICKUP;
            email.getBody().setOrderStatus(status);
            List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStoreDeliveryTypeAndStorePickup(verticalId, OrderStatus.AWAITING_PICKUP.name(), storeDeliveryType, storePickup);            
            if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for status: AWAITING_PICKUP" + newStatus);                
            } else {        
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size());
                orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);
            }
            
            if (bodyOrderCompletionStatusUpdate.getTrackingUrl()!=null && !bodyOrderCompletionStatusUpdate.getTrackingUrl().equals("")) {
                OrderShipmentDetail orderShipmentDetail = order.getOrderShipmentDetail();            
                orderShipmentDetail.setCustomerTrackingUrl(bodyOrderCompletionStatusUpdate.getTrackingUrl());
                orderShipmentDetail.setTrackingNumber(bodyOrderCompletionStatusUpdate.getSpOrderId());
                orderShipmentDetailRepository.save(orderShipmentDetail);
                
                //if order is combined delivery
                if (order.getOrderPaymentDetail()!=null && order.getOrderPaymentDetail().getIsCombinedDelivery()) {
                    List<OrderPaymentDetail> orderPaymentDetailList = orderPaymentDetailRepository.findByDeliveryQuotationReferenceId(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId());
                    for (int z=0;z<orderPaymentDetailList.size();z++) {
                        String relatedOrderId = orderPaymentDetailList.get(z).getOrderId();
                        //check order status, if cancel no need to update tracking url
                        Optional<Order> optOrderRelated = orderRepository.findById(relatedOrderId);
                        if (optOrderRelated.isPresent() && optOrderRelated.get().getCompletionStatus()!=OrderStatus.CANCELED_BY_MERCHANT) {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Update same tracking url for orderId:"+relatedOrderId+" : "+bodyOrderCompletionStatusUpdate.getTrackingUrl());
                            orderShipmentDetailRepository.UpdateTrackingUrlAndSpOrderId(bodyOrderCompletionStatusUpdate.getTrackingUrl(), bodyOrderCompletionStatusUpdate.getSpOrderId(), relatedOrderId);
                        }
                    }
                }
            }            
        } else if (newStatus.contains("FAILED")) {
            //something failed in order processing
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Something failed! Not read config from db");
        } else {
            //normal flow
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Normal flow. Read config from db");
            List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, newStatus, storePickup, storeDeliveryType, order.getPaymentType());            
            if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for status: " + newStatus);
                
                orderProcessResult.httpStatus = HttpStatus.NOT_FOUND;
                orderProcessResult.errorMsg = "Status config not found for status: " + newStatus;
                return orderProcessResult;
            } else {        
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size());
                orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);
            }
                
            //check current status if in correct sequence
            OrderCompletionStatusConfig prevOrderCompletionStatusConfig = null;
            
            List<OrderCompletionStatusConfig> prevOrderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, previousStatus.name(), storePickup, storeDeliveryType, order.getPaymentType());
            if (prevOrderCompletionStatusConfigs == null || prevOrderCompletionStatusConfigs.isEmpty()) {
                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "prevOrderCompletionStatusConfigs not found!");
                if (status==OrderStatus.PAYMENT_CONFIRMED) {
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Not correct sequence");                    
                    orderProcessResult.httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    orderProcessResult.errorMsg = "Wrong status sent: " + newStatus;
                    return orderProcessResult;
                }
            } else {
                prevOrderCompletionStatusConfig = prevOrderCompletionStatusConfigs.get(0);
                int prevSequence = prevOrderCompletionStatusConfig.getStatusSequence();
                int newSequence = prevSequence + 1;
                int orderSequence = orderCompletionStatusConfig.getStatusSequence();
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "prev status: " + prevOrderCompletionStatusConfig.getStatus()+" sequence:"+prevOrderCompletionStatusConfig.getStatusSequence());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "current status: " + orderCompletionStatusConfig.getStatus()+" sequence:"+orderCompletionStatusConfig.getStatusSequence());
                if (orderSequence != newSequence) {
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "New Sequence not correct! CorrectSequence:"+newSequence+" OrderSequence:"+orderSequence);
                    Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for status: " + newStatus);
                    
                    orderProcessResult.httpStatus = HttpStatus.NOT_ACCEPTABLE;
                    orderProcessResult.errorMsg = "Wrong status sent: " + newStatus;
                    return orderProcessResult;
                }
            }
        }
        //update order to being process
        orderRepository.UpdateOrderBeingProcess(orderId);
        double refundAmount=0.00;
        
        switch (status) {
            case PAYMENT_CONFIRMED:
                //clear cart item
                cartItemRepository.clearCartItem(order.getCartId());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cleared cartItem for cartId: " + order.getCartId());
                
                //deduct customer voucher 
                if (order.getStoreVoucherId()!=null) {
                    voucherRepository.deductVoucherBalance(order.getStoreVoucherId());                                        
                    CustomerVoucher customerVoucher = customerVoucherRepository.findByCustomerIdAndVoucherId(order.getCustomerId(), order.getStoreVoucherId());
                    if (customerVoucher!=null) {
                        customerVoucher.setIsUsed(true);
                        customerVoucherRepository.save(customerVoucher);
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Removed customer voucherId: " + order.getStoreVoucherId());
                    } 
                }
                
                //check platform voucher in group order
                if (order.getOrderGroupId()!=null && order.getPaymentType().equals(StorePaymentType.ONLINEPAYMENT.name())) {
                    //deduct platform voucher
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Find order groupId:"+order.getOrderGroupId());
                    Optional<OrderGroup> orderGroup = orderGroupRepository.findById(order.getOrderGroupId());
                    if (orderGroup.isPresent()) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order Group found:"+orderGroup.get().getId());
                        CustomerVoucher customerVoucher = customerVoucherRepository.findByCustomerIdAndVoucherId(order.getCustomerId(), orderGroup.get().getPlatformVoucherId());
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order Group found:"+orderGroup.get().getId());
                        if (customerVoucher!=null && customerVoucher.getIsUsed()==false) {
                            customerVoucher.setIsUsed(true);
                            customerVoucherRepository.save(customerVoucher);
                            voucherRepository.deductVoucherBalance(orderGroup.get().getPlatformVoucherId());                                                                
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Removed customer group voucherId: " + orderGroup.get().getPlatformVoucherId());
                        } 
                        
                        //update for guest                        
                        List<CustomerVoucher> guestVoucherList = customerVoucherRepository.findByGuestEmailAndVoucherId(order.getOrderShipmentDetail().getEmail(), orderGroup.get().getPlatformVoucherId());
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Guest voucher found:"+guestVoucherList.size());
                        if (guestVoucherList.size()>0 && guestVoucherList.get(0).getIsUsed()==false) {
                            CustomerVoucher guestVoucher = guestVoucherList.get(0);
                            guestVoucher.setIsUsed(true);
                            customerVoucherRepository.save(guestVoucher);
                            voucherRepository.deductVoucherBalance(orderGroup.get().getPlatformVoucherId());                                                                
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Removed guest group voucherId: " + orderGroup.get().getPlatformVoucherId());
                        } 
                    }
                    
                    //update payment status for order group 
                    OrderGroup orderGroupData = orderGroup.get();
                    orderGroupData.setPaymentStatus("PAID");
                    orderGroupData.setPaidAmount(orderGroupData.getTotal());
                    orderGroupRepository.save(orderGroupData);
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order Group paymentStatus updated to PAID for groupId:"+orderGroupData.getId());
                }
                
                //update status
                bodyOrderCompletionStatusUpdate.setStatus(OrderStatus.PAYMENT_CONFIRMED);
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setCompletionStatus(OrderStatus.PAYMENT_CONFIRMED);
                insertOrderCompletionStatusUpdate(OrderStatus.PAYMENT_CONFIRMED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                insertOrderPaymentStatusUpdate(PaymentStatus.PAID, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatusUpdate created with orderId: " + orderId);
                //inserting order completing status update

                try {
                    ProductInventory productInventory;
                    Product product;
                    //sending request to rocket chat for posting order
                    for (int i = 0; i < orderItems.size(); i++) {
                        // get product details

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
                                //productInventory = productService.changeProductStatus(order.getStoreId(), orderItems.get(i).getProductId(), orderItems.get(i).getItemCode(), ProductStatus.OUTOFSTOCK);
                                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "this product variant is out of stock now storeId: " + order.getStoreId() + ", productId: " + orderItems.get(i).getProductId() + ", itemCode: " + orderItems.get(i).getItemCode());
                            }
                        } else {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Product tracking is not enabled by marchant");
                        }

                        //reduce quantity of product inventory
                    }

                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order posted to rocket chat");
                } catch (Exception ex) {
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Exception occur ", ex);
                }

                break;                        
                
            case BEING_PREPARED:
                insertOrderCompletionStatusUpdate(OrderStatus.BEING_PREPARED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                order.setCompletionStatus(OrderStatus.BEING_PREPARED);
                break;

            case AWAITING_PICKUP:
                insertOrderCompletionStatusUpdate(OrderStatus.AWAITING_PICKUP, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId, bodyOrderCompletionStatusUpdate.getDate(), bodyOrderCompletionStatusUpdate.getTime());
                order.setCompletionStatus(OrderStatus.AWAITING_PICKUP);
                break;
                
            case BEING_DELIVERED:
                insertOrderCompletionStatusUpdate(OrderStatus.BEING_DELIVERED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                order.setCompletionStatus(OrderStatus.BEING_DELIVERED);
                break;

            case DELIVERED_TO_CUSTOMER:
                insertOrderCompletionStatusUpdate(OrderStatus.DELIVERED_TO_CUSTOMER, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                order.setCompletionStatus(OrderStatus.DELIVERED_TO_CUSTOMER);
                break;

            case READY_FOR_DELIVERY:
                insertOrderCompletionStatusUpdate(OrderStatus.READY_FOR_DELIVERY, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                order.setCompletionStatus(OrderStatus.READY_FOR_DELIVERY);
                break;

            case REJECTED_BY_STORE:
                insertOrderCompletionStatusUpdate(OrderStatus.REJECTED_BY_STORE, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                order.setCompletionStatus(OrderStatus.REJECTED_BY_STORE);
                break;
                
            case PAYMENT_FAILED:
                insertOrderCompletionStatusUpdate(OrderStatus.PAYMENT_FAILED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                order.setPaymentStatus(PaymentStatus.FAILED);
                order.setCompletionStatus(OrderStatus.PAYMENT_FAILED);
                insertOrderPaymentStatusUpdate(PaymentStatus.FAILED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                break;
                
            case FAILED:
                insertOrderCompletionStatusUpdate(OrderStatus.FAILED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                order.setCompletionStatus(OrderStatus.FAILED);
                break; 
            
            case FAILED_FIND_DRIVER:
                insertOrderCompletionStatusUpdate(OrderStatus.FAILED_FIND_DRIVER, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                order.setCompletionStatus(previousStatus);
                break;
                
            case CANCELED_BY_MERCHANT:
                insertOrderCompletionStatusUpdate(OrderStatus.FAILED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                order.setCompletionStatus(OrderStatus.FAILED);
                
                //update statut to cancel
                orderRepository.CancelOrder(order.getId(), OrderStatus.CANCELED_BY_MERCHANT, new Date());
                email.setFrom(financeEmailAddress);
                email.setFromName(financeEmailSenderName);
                
                Optional<PaymentOrder> optPayment = paymentOrderRepository.findByClientTransactionId("G"+order.getOrderGroupId());
                if (!optPayment.isPresent()) {
                    //find individual order
                    optPayment = paymentOrderRepository.findByClientTransactionId(order.getId());
                }
                if (optPayment.isPresent()) {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Payment Order found with orderId: " + order.getId());
                    //create refund record
                    refundAmount=order.getTotal();
                    OrderRefund orderRefund = new OrderRefund();
                    orderRefund.setOrderId(order.getId());
                    orderRefund.setRefundType(RefundType.ORDER_CANCELLED);
                    orderRefund.setPaymentChannel(optPayment.get().getPaymentChannel());
                    orderRefund.setRefundAmount(order.getTotal());
                    orderRefund.setRefundStatus(RefundStatus.PENDING);
                    orderRefundRepository.save(orderRefund);
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "refund record created for orderId: " + order.getId());
                } else {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Payment Order NOT found with orderId: " + order.getId());
                }
                
                //if combined delivery, need to inform delivery-service
                if (order.getOrderPaymentDetail().getIsCombinedDelivery()) {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Inform delivery-service on canceled order");
                    try {
                        DeliveryResponse deliveryResponse = deliveryService.confirmOrderDelivery(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId(), 
                                order.getId(), 
                                bodyOrderCompletionStatusUpdate.getDate(), 
                                bodyOrderCompletionStatusUpdate.getTime());
                    } catch (Exception ex) {
                        Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Exception occur while inform delivery-service : ", ex);
                    }                    
                }
                
                //revert voucher if any
                if (order.getStoreVoucherId()!=null) {
                    CustomerVoucher customerVoucher = customerVoucherRepository.findByCustomerIdAndVoucherId(order.getCustomerId(), order.getStoreVoucherId());
                    if (customerVoucher!=null) {
                        customerVoucher.setIsUsed(false);
                        customerVoucherRepository.save(customerVoucher);
                        voucherRepository.addVoucherBalance(order.getStoreVoucherId());   
                        order.setStoreVoucherId(null);
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Revert customer voucherId: " + order.getStoreVoucherId());
                    } 
                }
                if (order.getOrderGroupId()!=null) {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Find order groupId:"+order.getOrderGroupId());
                    Optional<OrderGroup> orderGroup = orderGroupRepository.findById(order.getOrderGroupId());
                    if (orderGroup.isPresent()) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order Group found:"+orderGroup.get().getId());
                        CustomerVoucher customerVoucher = customerVoucherRepository.findByCustomerIdAndVoucherId(order.getCustomerId(), orderGroup.get().getPlatformVoucherId());
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "customerVoucher found:"+customerVoucher);
                        if (customerVoucher!=null && customerVoucher.getIsUsed()==true) {
                            customerVoucher.setIsUsed(false);
                            customerVoucherRepository.save(customerVoucher);
                            voucherRepository.addVoucherBalance(orderGroup.get().getPlatformVoucherId());
                            OrderGroup orderG = orderGroup.get();
                            orderG.setPlatformVoucherId(null);
                            orderGroupRepository.save(orderG);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Revert customer group voucherId: " + orderGroup.get().getPlatformVoucherId());
                        } 
                        
                        //update for guest                        
                        List<CustomerVoucher> guestVoucherList = customerVoucherRepository.findByGuestEmailAndVoucherId(order.getOrderShipmentDetail().getEmail(), orderGroup.get().getPlatformVoucherId());
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Guest voucher found:"+guestVoucherList.size());
                        if (guestVoucherList.size()>0 && guestVoucherList.get(0).getIsUsed()==true) {
                            CustomerVoucher guestVoucher = guestVoucherList.get(0);
                            guestVoucher.setIsUsed(false);
                            customerVoucherRepository.save(guestVoucher);
                            voucherRepository.addVoucherBalance(orderGroup.get().getPlatformVoucherId());                                                                
                            OrderGroup orderG = orderGroup.get();
                            orderG.setPlatformVoucherId(null);
                            orderGroupRepository.save(orderG);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Revert guest group voucherId: " + orderGroup.get().getPlatformVoucherId());
                        } 
                    }
                }
            default:
               order.setCompletionStatus(status);
               insertOrderCompletionStatusUpdate(status, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
        }
        
        if (orderCompletionStatusConfig!=null) {
            OrderShipmentDetail orderShipmentDetail = orderShipmentDetailRepository.findByOrderId(orderId);
            Optional<PaymentOrder> optPaymentDetails = null;
            if (order.getOrderGroupId()!=null) {
                optPaymentDetails = paymentOrderRepository.findByClientTransactionId("G"+order.getOrderGroupId());
                if (!optPaymentDetails.isPresent()) {
                    optPaymentDetails = paymentOrderRepository.findByClientTransactionId(orderId);
                }
            } else {
                optPaymentDetails = paymentOrderRepository.findByClientTransactionId(orderId);
            }
            PaymentOrder paymentDetails = null;
            if (optPaymentDetails.isPresent()) {
                paymentDetails = optPaymentDetails.get();
            }
            //request delivery
            orderProcessResult.pendingRequestDelivery=false;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "request delivery: " + orderCompletionStatusConfig.getRequestDelivery());
            if (orderCompletionStatusConfig.getRequestDelivery() && proceedRequestDelivery && newStatus.equals("ASSIGNING_DRIVER")==false) {
                try {
                    DeliveryResponse deliveryResponse = deliveryService.confirmOrderDelivery(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId(), 
                            order.getId(), 
                            bodyOrderCompletionStatusUpdate.getDate(), 
                            bodyOrderCompletionStatusUpdate.getTime());
                    
                    if (deliveryResponse!=null) {
                        if (deliveryResponse.getStatus().equals("ASSIGNING_DRIVER") || deliveryResponse.getStatus().equals("NEW_ORDER") || deliveryResponse.getStatus().equals("ASSIGNING_RIDER")) {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "OrderCreated:"+deliveryResponse.getOrderCreated().toString());
                            DeliveryOrder deliveryOrder = (DeliveryOrder)deliveryResponse.getOrderCreated();
                            status = OrderStatus.AWAITING_PICKUP;
                            email.getBody().setOrderStatus(status);
                            email.getBody().setMerchantTrackingUrl(deliveryOrder.getMerchantTrackingUrl());
                            email.getBody().setCustomerTrackingUrl(deliveryOrder.getCustomerTrackingUrl());
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "delivery confirmed for order: " + orderId + "awaiting for pickup");

                            orderShipmentDetail.setMerchantTrackingUrl(deliveryOrder.getMerchantTrackingUrl());
                            orderShipmentDetail.setCustomerTrackingUrl(deliveryOrder.getCustomerTrackingUrl());
                            orderShipmentDetail.setTrackingNumber(deliveryOrder.getSpOrderId());
                            orderShipmentDetailRepository.save(orderShipmentDetail);
                            
                            //if order is combined delivery
                            if (deliveryOrder.getCustomerTrackingUrl()!=null && order.getOrderPaymentDetail()!=null && order.getOrderPaymentDetail().getIsCombinedDelivery()) {
                                List<OrderPaymentDetail> orderPaymentDetailList = orderPaymentDetailRepository.findByDeliveryQuotationReferenceId(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId());
                                for (int z=0;z<orderPaymentDetailList.size();z++) {
                                    String relatedOrderId = orderPaymentDetailList.get(z).getOrderId();
                                    //check order status, if cancel no need to update tracking url
                                    Optional<Order> optOrderRelated = orderRepository.findById(relatedOrderId);
                                    if (optOrderRelated.isPresent() && optOrderRelated.get().getCompletionStatus()!=OrderStatus.CANCELED_BY_MERCHANT) {
                                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Update same tracking url for orderId:"+relatedOrderId+" : "+deliveryOrder.getCustomerTrackingUrl());
                                    orderShipmentDetailRepository.UpdateTrackingUrlAndSpOrderId(deliveryOrder.getCustomerTrackingUrl(), bodyOrderCompletionStatusUpdate.getSpOrderId(), relatedOrderId);
                                    }                                    
                                }
                            }
                
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "added tracking urls to orderId:" + orderId);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "delivery confirmed for order: " + orderId + "awaiting for pickup");
                        } else if (deliveryResponse.getStatus().equals("FAILED")) {
                            //failed
                            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error while confirming order Delivery. deliveryOrder is null ");
                            insertOrderCompletionStatusUpdate(OrderStatus.REQUESTING_DELIVERY_FAILED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);                        
                            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Revert to previous status:"+previousStatus);
                            order.setCompletionStatus(previousStatus);
                            //update order to finish process
                            orderRepository.UpdateOrderFinishProcess(orderId);

                            orderProcessResult.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                            orderProcessResult.errorMsg = "Requesting delivery failed";
                            return orderProcessResult;
                        } else if (deliveryResponse.getStatus().equals("PENDING")) {
                            //pending
                            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Confirming order Delivery is still pending");                            
                            orderProcessResult.httpStatus = HttpStatus.ACCEPTED;
                            orderProcessResult.errorMsg = "Requesting delivery is pending";
                            orderProcessResult.data = order;
                            orderProcessResult.previousStatus = previousStatus;
                            orderProcessResult.orderCompletionStatusConfig = orderCompletionStatusConfig;
                            orderProcessResult.email = email;
                            orderProcessResult.storeWithDetails = storeWithDetails;
                            return orderProcessResult;
                        }
                    } else {
                        Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error while confirming order Delivery. deliveryOrder is null ");
                        insertOrderCompletionStatusUpdate(OrderStatus.REQUESTING_DELIVERY_FAILED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);                        
                        Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Revert to previous status:"+previousStatus);
                        order.setCompletionStatus(previousStatus);
                        //update order to finish process
                        orderRepository.UpdateOrderFinishProcess(orderId);
                       
                        orderProcessResult.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                        orderProcessResult.errorMsg = "Requesting delivery failed";
                        return orderProcessResult;
                    }
                } catch (Exception ex) {
                    //there might be some issue so need to updated email for issue and refund
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Exception occur while confirming order Delivery ", ex);
                    insertOrderCompletionStatusUpdate(OrderStatus.REQUESTING_DELIVERY_FAILED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Revert to previous status:"+previousStatus);
                    order.setCompletionStatus(previousStatus);                        
                    //update order to finish process
                    orderRepository.UpdateOrderFinishProcess(orderId);
                    
                    orderProcessResult.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                    orderProcessResult.errorMsg = "Requesting delivery failed";
                    return orderProcessResult;
                }
            } else if (orderCompletionStatusConfig.getRequestDelivery()) {
                orderProcessResult.pendingRequestDelivery=true;
            }

            if (orderProcessResult.pendingRequestDelivery==false || newStatus.equals("ASSIGNING_DRIVER")) {
                            
                //send email to customer if config allows
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "email to customer: " + orderCompletionStatusConfig.getEmailToCustomer());
                if (orderCompletionStatusConfig.getEmailToCustomer()) {
                    String emailContent = orderCompletionStatusConfig.getCustomerEmailContent();
                    if (emailContent != null) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "email content is not null");
                        //sending email
                        try {
                            RegionCountry regionCountry = null;
                            Optional<RegionCountry> t = regionCountriesRepository.findById(storeWithDetails.getRegionCountryId());
                            if (t.isPresent()) {
                                regionCountry = t.get();
                            }
                            //get customer info
                            Optional<Customer> customerOpt = customerRepository.findById(order.getCustomerId());
                            boolean sendActivationLink = false;
                            String customerEmail = null;
                            if (customerOpt.isPresent()) {
                                Customer customer = customerOpt.get();
                                if (customer.getIsActivated()==false) {
                                    sendActivationLink=true;
                                }
                                customerEmail = customer.getEmail();
                            }
                            String deliveryChargesRemarks="";
                            if (order.getOrderPaymentDetail().getIsCombinedDelivery()) {
                                List<OrderPaymentDetail> orderPaymentDetailList = orderPaymentDetailRepository.findByDeliveryQuotationReferenceId(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId());
                                deliveryChargesRemarks = " (combined x"+orderPaymentDetailList.size()+" shops)";
                            }
                            emailContent = MessageGenerator.generateEmailContent(emailContent, order, storeWithDetails, orderItems, orderShipmentDetail, paymentDetails, regionCountry, sendActivationLink, storeWithDetails.getRegionVertical().getCustomerActivationNotice(), customerEmail, assetServiceBaseUrl, deliveryChargesRemarks, 0.00);
                            email.setRawBody(emailContent);
                            emailService.sendEmail(email);
                        } catch (Exception ex) {
                            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error sending email :", ex);
                        }
                    } else {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "email content is null");
                    }
                }                
                        
                //send email to finance if config allows
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "email to finance: " + orderCompletionStatusConfig.getEmailToFinance());
                if (orderCompletionStatusConfig.getEmailToFinance()) {
                    String emailContent = orderCompletionStatusConfig.getFinanceEmailContent();
                    if (emailContent != null) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "finance email is not null");
                        //sending email
                        try {
                            RegionCountry regionCountry = null;
                            Optional<RegionCountry> t = regionCountriesRepository.findById(storeWithDetails.getRegionCountryId());
                            if (t.isPresent()) {
                                regionCountry = t.get();
                            }
                            String[] emailAddress = {financeEmailAddress};
                            email.setFrom(null);
                            email.setFromName(financeEmailSenderName);
                            email.setTo(emailAddress);
                            String deliveryChargesRemarks="";
                            if (order.getOrderPaymentDetail().getIsCombinedDelivery()) {
                                List<OrderPaymentDetail> orderPaymentDetailList = orderPaymentDetailRepository.findByDeliveryQuotationReferenceId(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId());
                                deliveryChargesRemarks = " (combined x"+orderPaymentDetailList.size()+" shops)";
                            }
                            emailContent = MessageGenerator.generateEmailContent(emailContent, order, storeWithDetails, orderItems, orderShipmentDetail, paymentDetails, regionCountry, false, null, null, assetServiceBaseUrl, deliveryChargesRemarks, 0.00);
                            email.setRawBody(emailContent);
                            emailService.sendEmail(email);
                        } catch (Exception ex) {
                            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error sending email :", ex);
                        }
                    } else {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "finance email content is null");
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
                        fcmService.sendPushNotification(order, storeWithDetails.getId(), storeWithDetails.getName(), pushNotificationTitle, pushNotificationContent, status, storeWithDetails.getRegionVertical().getDomain());
                    } catch (Exception e) {
                        Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "pushNotificationToMerchat error ", e);
                    }

                }
            
                //send push notification to WA alert to admin
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getPushWAToAdmin to store: " + orderCompletionStatusConfig.getPushWAToAdmin());
                if (orderCompletionStatusConfig.getPushWAToAdmin()) {
                    try {
                        //String storeName, String invoiceNo, String orderId, String merchantToken
                        whatsappService.sendAdminAlert(status.name(), storeWithDetails.getName(), order.getInvoiceId(), order.getId(), DateTimeUtil.currentTimestamp());
                    } catch (Exception e) {
                        Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "pushNotificationToMerchat error ", e);
                    }

                }
                
                //send push notification to WA alert to customer
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getPushWAToCustomer to store: " + orderCompletionStatusConfig.getPushWAToCustomer());
                if (orderCompletionStatusConfig.getPushWAToCustomer()) {
                    try {
                        //String storeName, String invoiceNo, String orderId, String merchantToken
                        //get customer info
                        OrderShipmentDetail orderShipmentDetails = orderShipmentDetailRepository.findByOrderId(orderId);
                        if (orderShipmentDetails!=null) {                            
                            String customerMsisdn = orderShipmentDetails.getPhoneNumber();                        
                            String invoicePdf = invoiceBaseUrl+"/"+order.getId();
                            //get customer info
                            Optional<Customer> customerOpt = customerRepository.findById(order.getCustomerId());
                            boolean isRegisteredUser = false;
                            if (customerOpt.isPresent()) {
                                Customer customer = customerOpt.get();
                                if (customer.getIsActivated()) {
                                    isRegisteredUser=true;
                                }
                            }
                            whatsappService.sendCustomerAlert(customerMsisdn, status.name(), storeWithDetails.getName(), order.getInvoiceId(), order.getId(), DateTimeUtil.currentTimestamp(), orderCompletionStatusConfig.getPushWAToCustomerTemplateName(), orderCompletionStatusConfig.getPushWAToCustomerTemplateFormat(), storeWithDetails.getCity(), invoicePdf, isRegisteredUser);
                        } else {
                            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order shipment details not found");
                        }
                    } catch (Exception e) {
                        Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "pushNotificationToMerchat error ", e);
                    }

                }
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Not done with RequestDelivery");
            }
        }
                
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatusUpdate updated for orderId: " + orderId + ", with orderStatus: " + status.toString());
        orderRepository.save(order);
         
        //update order to finish process
        orderRepository.UpdateOrderFinishProcess(orderId);
        
        orderProcessResult.httpStatus = HttpStatus.ACCEPTED;        
        orderProcessResult.data = order;
        orderProcessResult.previousStatus = previousStatus;
        orderProcessResult.orderCompletionStatusConfig = orderCompletionStatusConfig;
        orderProcessResult.email = email;
        orderProcessResult.storeWithDetails = storeWithDetails;
        
        //get next action
        if (orderCompletionStatusConfig!=null) {
            OrderCompletionStatusConfig nextCompletionStatusConfig = null;
            int nextSequence = orderCompletionStatusConfig.getStatusSequence()+1;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Find config for next status VerticalId:"+verticalId+" NextSequence:"+nextSequence+" storePickup:"+storePickup+" deliveryType:"+storeDeliveryType+" paymentType:"+order.getPaymentType());
            List<OrderCompletionStatusConfig> nextActionCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusSequenceAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, nextSequence, storePickup, storeDeliveryType, order.getPaymentType());
            if (nextActionCompletionStatusConfigs != null && !nextActionCompletionStatusConfigs.isEmpty()) {           
                nextCompletionStatusConfig = nextActionCompletionStatusConfigs.get(0);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Next action status: " + nextCompletionStatusConfig.getStatus()+" sequence:"+nextCompletionStatusConfig.getStatusSequence());
                order.setNextCompletionStatus(nextCompletionStatusConfig.status);
                order.setNextActionText(nextCompletionStatusConfig.nextActionText);                
            }
        }
        orderProcessResult.data = order;
        
        return orderProcessResult;
        
    }
    
    void insertOrderPaymentStatusUpdate(PaymentStatus paymentStatus, String comments, String modifiedBy, String orderId) {
        String logprefix = "insertOrderPaymentStatusUpdate";
        OrderPaymentStatusUpdate orderPaymentStatusUpdate = new OrderPaymentStatusUpdate();
        orderPaymentStatusUpdate.setStatus(paymentStatus);
        orderPaymentStatusUpdate.setComments(comments);
        orderPaymentStatusUpdate.setModifiedBy(modifiedBy);
        orderPaymentStatusUpdate.setOrderId(orderId);
        orderPaymentStatusUpdateRepository.save(orderPaymentStatusUpdate);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "inserted orderPaymentStatusUpdate: " + paymentStatus + " for orderId: " + orderId);

    }

    void insertOrderCompletionStatusUpdate(OrderStatus completionStatus, String comments, String modifiedBy, String orderId) {
        String logprefix = "insertOrderCompletionStatusUpdate";
        OrderCompletionStatusUpdate orderPaymentStatusUpdate = new OrderCompletionStatusUpdate();
        orderPaymentStatusUpdate.setStatus(completionStatus);
        orderPaymentStatusUpdate.setComments(comments);
        orderPaymentStatusUpdate.setModifiedBy(modifiedBy);
        orderPaymentStatusUpdate.setOrderId(orderId);
        orderCompletionStatusUpdateRepository.save(orderPaymentStatusUpdate);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "inserted orderPaymentStatusUpdate: " + completionStatus + " for orderId: " + orderId);

    }
    
    void insertOrderCompletionStatusUpdate(OrderStatus completionStatus, String comments, String modifiedBy, String orderId, String pickupDate, String pickupTime) {
        String logprefix = "insertOrderCompletionStatusUpdate";
        OrderCompletionStatusUpdate orderPaymentStatusUpdate = new OrderCompletionStatusUpdate();
        orderPaymentStatusUpdate.setStatus(completionStatus);
        orderPaymentStatusUpdate.setComments(comments);
        orderPaymentStatusUpdate.setModifiedBy(modifiedBy);
        orderPaymentStatusUpdate.setOrderId(orderId);
        orderPaymentStatusUpdate.setPickupDate(pickupDate);
        orderPaymentStatusUpdate.setPickupTime(pickupTime);
        orderCompletionStatusUpdateRepository.save(orderPaymentStatusUpdate);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "inserted orderPaymentStatusUpdate: " + completionStatus + " for orderId: " + orderId);

    }
}
