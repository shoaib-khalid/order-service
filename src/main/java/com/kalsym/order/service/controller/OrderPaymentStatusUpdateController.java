package com.kalsym.order.service.controller;

import com.google.common.collect.Lists;
import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.enums.ProductStatus;
import com.kalsym.order.service.enums.RefundStatus;
import com.kalsym.order.service.enums.RefundType;
import com.kalsym.order.service.enums.StorePaymentType;
import com.kalsym.order.service.model.Body;
import com.kalsym.order.service.model.Email;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.repository.*;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderPaymentStatusUpdate;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.service.EmailService;
import com.kalsym.order.service.service.OrderPostService;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.MessageGenerator;
import com.kalsym.order.service.utility.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kalsym.order.service.model.DeliveryOrder;
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.model.*;
import com.kalsym.order.service.service.FCMService;
import com.kalsym.order.service.utility.Logger;
import java.util.Date;

/**
 * @author 7cu
 */
@RestController()
@RequestMapping("/orders/{orderId}/completion-status-updates")
public class OrderPaymentStatusUpdateController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    DeliveryService deliveryService;

    @Autowired
    FCMService fcmService;

    @Autowired
    EmailService emailService;

    @Autowired
    ProductService productService;

    @Autowired
    OrderPostService orderPostService;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderShipmentDetailRepository orderShipmentDetailRepository;

    @Autowired
    OrderPaymentStatusUpdateRepository orderPaymentStatusUpdateRepository;

    @Autowired
    OrderCompletionStatusUpdateRepository orderCompletionStatusUpdateRepository;

    @Autowired
    StoreDetailsRepository storeDetailsRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    OrderCompletionStatusConfigRepository orderCompletionStatusConfigRepository;

    @Autowired
    ProductInventoryRepository productInventoryRepository;
    
    @Autowired
    RegionCountriesRepository regionCountriesRepository;
    
    @Autowired
    OrderRefundRepository orderRefundRepository;
    
    @Autowired
    PaymentOrderRepository paymentOrderRepository;
    
    @Value("${onboarding.order.URL:https://symplified.biz/orders/order-details?orderId=}")
    private String onboardingOrderLink;
    
    @Value("${finance.email.address:finance@symplified.com}")
    private String financeEmailAddress;

    @PutMapping(path = {""}, name = "order-completion-status-updates-put-by-order-id")
    @PreAuthorize("hasAnyAuthority('order-completion-status-updates-put-by-order-id', 'all')")
    public ResponseEntity<HttpResponse> putOrderCompletionStatusUpdatesConfirm(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @Valid @RequestBody OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-completion-status-updates-confirm-put-by-order-id, orderId: " + orderId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderCompletionStatusUpdate.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: " + orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND, "order not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Order order = optOrder.get();
        Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(order.getStoreId());
        if (!optStore.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store not found with storeId: " + order.getStoreId());
            response.setErrorStatus(HttpStatus.NOT_FOUND, "Store not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        if (order.getBeingProcess()!=null) {
            if (order.getBeingProcess()) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order is being processed. orderId: " + orderId);
                response.setErrorStatus(HttpStatus.CONFLICT, "Order is being processed");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
        }
  
        StoreWithDetails storeWithDetails = optStore.get();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store details got : " + storeWithDetails.toString());
//        String status = bodyOrderCompletionStatusUpdate.getStatus();
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
        body.setLogoUrl(storeWithDetails.getStoreAsset() == null ? "" : storeWithDetails.getStoreAsset().getLogoUrl());

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

        String newStatus = bodyOrderCompletionStatusUpdate.getStatus().toString();
        String verticalId = storeWithDetails.getVerticalCode();
        Boolean storePickup = order.getOrderShipmentDetail().getStorePickup();
        String storeDeliveryType = storeWithDetails.getStoreDeliveryDetail().getType();
        newStatus = newStatus.replace(" ", "_");
        OrderStatus previousStatus = order.getCompletionStatus();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "prevStatus:"+previousStatus+" newStatus:"+newStatus+" CompletionCriteria = [verticalId:"+verticalId+" storePickup:"+storePickup+" storeDeliveryType: " + storeDeliveryType+" orderPaymentType:"+order.getPaymentType()+"]");        
        
        OrderCompletionStatusConfig orderCompletionStatusConfig = null;
        
        if (newStatus.contains("FAILED")) {
            //something failed in order processing
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Something failed! Not read config from db");
        } else if (newStatus.contains("CANCELED_BY_MERCHANT")) {
            //cancel order
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Merchant Cancel Order. Read config from db");
            List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndPaymentType(verticalId, newStatus, order.getPaymentType());            
            if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for status: " + newStatus);                
            } else {        
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size());
                orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);
            }
        } else {
            //normal flow
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Normal flow. Read config from db");
            List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, newStatus, storePickup, storeDeliveryType, order.getPaymentType());            
            if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for status: " + newStatus);
                response.setSuccessStatus(HttpStatus.NOT_FOUND);
                response.setMessage("Status config not found for status: " + newStatus);
                response.setError("Status config not found for status: " + newStatus);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);            
            } else {        
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size());
                orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);
            }
                
            //check current status if in correct sequence
            OrderCompletionStatusConfig prevOrderCompletionStatusConfig = null;
            
            List<OrderCompletionStatusConfig> prevOrderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, previousStatus.name(), storePickup, storeDeliveryType, order.getPaymentType());
            if (prevOrderCompletionStatusConfigs == null || prevOrderCompletionStatusConfigs.isEmpty()) {
                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "prevOrderCompletionStatusConfigs not found!");
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
                    response.setSuccessStatus(HttpStatus.NOT_ACCEPTABLE);
                    response.setMessage("Wrong status sent: " + newStatus);
                    response.setError("Wrong status sent: " + newStatus);
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
                }
            }
        }
        //update order to being process
        orderRepository.UpdateOrderBeingProcess(orderId);
        
        switch (status) {
            case PAYMENT_CONFIRMED:
                //clear cart item
                cartItemRepository.clearCartItem(order.getCartId());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cleared cartItem for cartId: " + order.getCartId());

                //update status
                bodyOrderCompletionStatusUpdate.setStatus(OrderStatus.PAYMENT_CONFIRMED);
                order.setPaymentStatus(PaymentStatus.PAID);
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
                insertOrderCompletionStatusUpdate(OrderStatus.AWAITING_PICKUP, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
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
                
            case CANCELED_BY_MERCHANT:
                insertOrderCompletionStatusUpdate(OrderStatus.FAILED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                order.setCompletionStatus(OrderStatus.FAILED);
                
                //update statut to cancel
                orderRepository.CancelOrder(order.getId(), OrderStatus.CANCELED_BY_MERCHANT, new Date());
                
                Optional<PaymentOrder> optPayment = paymentOrderRepository.findByClientTransactionId(order.getId());
                if (optPayment.isPresent()) {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Payment Order found with orderId: " + order.getId());
                    //create refund record
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

        }
        
        if (orderCompletionStatusConfig!=null) {
            OrderShipmentDetail orderShipmentDetail = orderShipmentDetailRepository.findByOrderId(orderId);
            //request delivery
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "request delivery: " + orderCompletionStatusConfig.getRequestDelivery());
            if (orderCompletionStatusConfig.getRequestDelivery()) {
                try {
                    DeliveryOrder deliveryOrder = deliveryService.confirmOrderDelivery(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId(), 
                            order.getId(), 
                            bodyOrderCompletionStatusUpdate.getPickupDate(), 
                            bodyOrderCompletionStatusUpdate.getPickupTime());
                    if (deliveryOrder!=null) {
                        status = OrderStatus.AWAITING_PICKUP;
                        email.getBody().setMerchantTrackingUrl(deliveryOrder.getMerchantTrackingUrl());
                        email.getBody().setCustomerTrackingUrl(deliveryOrder.getCustomerTrackingUrl());
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "delivery confirmed for order: " + orderId + "awaiting for pickup");

                        orderShipmentDetail.setMerchantTrackingUrl(deliveryOrder.getMerchantTrackingUrl());
                        orderShipmentDetail.setCustomerTrackingUrl(deliveryOrder.getCustomerTrackingUrl());
                        orderShipmentDetailRepository.save(orderShipmentDetail);

                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "added tracking urls to orderId:" + orderId);
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "delivery confirmed for order: " + orderId + "awaiting for pickup");
                    } else {
                        Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error while confirming order Delivery. deliveryOrder is null ");
                        insertOrderCompletionStatusUpdate(OrderStatus.REQUESTING_DELIVERY_FAILED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);                        
                        Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Revert to previous status:"+previousStatus);
                        order.setCompletionStatus(previousStatus);
                        //update order to finish process
                        orderRepository.UpdateOrderFinishProcess(orderId);
                        response.setSuccessStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                        response.setMessage("Requesting delivery failed");
                        response.setError("Requesting delivery failed");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);                        
                    }
                } catch (Exception ex) {
                    //there might be some issue so need to updated email for issue and refund
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Exception occur while confirming order Delivery ", ex);
                    insertOrderCompletionStatusUpdate(OrderStatus.REQUESTING_DELIVERY_FAILED, bodyOrderCompletionStatusUpdate.getComments(), bodyOrderCompletionStatusUpdate.getModifiedBy(), orderId);
                    response.setSuccessStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                    response.setMessage("Requesting delivery failed");
                    response.setError("Requesting delivery failed");
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Revert to previous status:"+previousStatus);
                    order.setCompletionStatus(previousStatus);                        
                    //update order to finish process
                    orderRepository.UpdateOrderFinishProcess(orderId);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);                                            
                }
            }

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
                        emailContent = MessageGenerator.generateEmailContent(emailContent, order, storeWithDetails, orderItems, orderShipmentDetail, regionCountry);
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
                        email.setTo(emailAddress);
                        emailContent = MessageGenerator.generateEmailContent(emailContent, order, storeWithDetails, orderItems, orderShipmentDetail, regionCountry);
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
                    fcmService.sendPushNotification(order, storeWithDetails.getId(), storeWithDetails.getName(), pushNotificationTitle, pushNotificationContent, status);
                } catch (Exception e) {
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "pushNotificationToMerchat error ", e);
                }

            }
        }
                
        order.setCompletionStatus(status);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatusUpdate updated for orderId: " + orderId + ", with orderStatus: " + status.toString());
        orderRepository.save(order);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(order);
        
        //update order to finish process
        orderRepository.UpdateOrderFinishProcess(orderId);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

    }

    @PutMapping(path = {"/request-delivery"}, name = "order-completion-status-updates-put-by-order-id-by-merchant")
    @PreAuthorize("hasAnyAuthority('order-completion-status-updates-put-by-order-id-by-merchant', 'all')")
    public ResponseEntity<HttpResponse> requestToDelivery(HttpServletRequest request,
            @PathVariable(required = true) String orderId) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());
        OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate = new OrderCompletionStatusUpdate();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-completion-status-updates-put-by-order-id-by-merchant, orderId: {}", orderId);
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderCompletionStatusUpdate.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND, "order not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Order order = optOrder.get();

        Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(order.getStoreId());
        if (!optStore.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store not found with storeId: {}", order.getStoreId());
            response.setErrorStatus(HttpStatus.NOT_FOUND, "Store not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        StoreWithDetails storeWithDetails = optStore.get();

        OrderStatus prevStatus = order.getCompletionStatus();
        if ((!prevStatus.toString().equalsIgnoreCase(OrderStatus.PAYMENT_CONFIRMED.toString()) && !storeWithDetails.getPaymentType().equalsIgnoreCase(StorePaymentType.COD.toString())) || storeWithDetails.getPaymentType().equalsIgnoreCase(StorePaymentType.COD.toString())) {
            // should return error with cause
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Previous status in not valid: {}", order.getCompletionStatus().toString());
            response.setErrorStatus(HttpStatus.BAD_REQUEST, "You can't change status because previous status is: " + order.getCompletionStatus().toString() + " and StorePaymentStatus is: " + storeWithDetails.getPaymentType());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store details got : " + storeWithDetails.toString());
//        String status = bodyOrderCompletionStatusUpdate.getStatus();
//        OrderStatus status = bodyOrderCompletionStatusUpdate.getStatus();
        OrderStatus status = OrderStatus.READY_FOR_DELIVERY;
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
        body.setLogoUrl(storeWithDetails.getStoreAsset() == null ? "" : storeWithDetails.getStoreAsset().getLogoUrl());

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

        bodyOrderCompletionStatusUpdate.setModifiedBy(storeWithDetails.getName());
        bodyOrderCompletionStatusUpdate.setStatus(status);
        bodyOrderCompletionStatusUpdate.setOrderId(orderId);
        //inserting order completing status update
        orderCompletionStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatusUpdate updated for orderId: {}, with orderStatus: {}", orderId, status.toString());
        try {

            DeliveryOrder deliveryOrder = deliveryService.confirmOrderDelivery(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId(), order.getId(), bodyOrderCompletionStatusUpdate.getPickupDate(), bodyOrderCompletionStatusUpdate.getPickupTime());
            status = OrderStatus.AWAITING_PICKUP;
            email.getBody().setMerchantTrackingUrl(deliveryOrder.getMerchantTrackingUrl());
            email.getBody().setCustomerTrackingUrl(deliveryOrder.getCustomerTrackingUrl());

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "delivery confirmed for order: {} awaiting for pickup", orderId);

            //TODO: 
            OrderShipmentDetail orderShipmentDetail = orderShipmentDetailRepository.findByOrderId(orderId);
            orderShipmentDetail.setMerchantTrackingUrl(deliveryOrder.getMerchantTrackingUrl());
            orderShipmentDetail.setCustomerTrackingUrl(deliveryOrder.getCustomerTrackingUrl());
            orderShipmentDetailRepository.save(orderShipmentDetail);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "delivery confirmed for order: {} awaiting for pickup", orderId);

        } catch (Exception ex) {
            //there might be some issue so need to updated email for issue and refund
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Exception occur while confirming order Delivery ", ex);
            status = OrderStatus.REQUESTING_DELIVERY_FAILED;
        }
        //sending email
        emailService.sendEmail(email);
        //update completion status in order
        order.setCompletionStatus(status);

//        //inserting order completion status updates
//        orderCompletionStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatusUpdate updated for orderId: {}, with orderStatus: {}", orderId, status.toString());
        orderRepository.save(order);

        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(order);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

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

}
