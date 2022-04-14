/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.model.Email;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
import com.kalsym.order.service.model.OrderPaymentDetail;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.object.OrderProcessResult;
import com.kalsym.order.service.model.object.DeliveryServiceBulkConfirmRequest;
import com.kalsym.order.service.model.object.DeliveryServiceBulkConfirmResponse;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.OrderCompletionStatusConfigRepository;
import com.kalsym.order.service.model.repository.OrderCompletionStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.OrderPaymentStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderRefundRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.model.repository.OrderShipmentDetailRepository;
import com.kalsym.order.service.model.repository.PaymentOrderRepository;
import com.kalsym.order.service.model.repository.ProductInventoryRepository;
import com.kalsym.order.service.model.repository.RegionCountriesRepository;
import com.kalsym.order.service.model.repository.StoreDetailsRepository;
import com.kalsym.order.service.model.repository.OrderPaymentDetailRepository;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.service.EmailService;
import com.kalsym.order.service.service.FCMService;
import com.kalsym.order.service.service.OrderPostService;
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.service.WhatsappService;
import com.kalsym.order.service.utility.DateTimeUtil;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.MessageGenerator;
import com.kalsym.order.service.model.OrderCompletionStatusConfig;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.PaymentOrder;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.utility.Utilities;
import com.kalsym.order.service.enums.DeliveryType;
import com.kalsym.order.service.model.Customer;
import com.kalsym.order.service.model.repository.CustomerRepository;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.http.HttpStatus;

/**
 *
 * @author taufik
 */
public class OrderProcessBulkThread extends Thread {
    
    private String logprefix;
    private final String financeEmailAddress;
    private final String financeEmailSenderName;
    private OrderCompletionStatusUpdate[] bodyOrderCompletionStatusUpdateList;
    private String onboardingOrderLink;
    
    private OrderRepository orderRepository;
    private StoreDetailsRepository storeDetailsRepository;
    private OrderItemRepository orderItemRepository;
    private OrderCompletionStatusConfigRepository orderCompletionStatusConfigRepository;
    private CartItemRepository cartItemRepository;
    private ProductInventoryRepository productInventoryRepository;
    private PaymentOrderRepository paymentOrderRepository;
    private OrderRefundRepository orderRefundRepository;
    private OrderShipmentDetailRepository orderShipmentDetailRepository;
    private RegionCountriesRepository regionCountriesRepository;
    private OrderPaymentStatusUpdateRepository orderPaymentStatusUpdateRepository;
    private OrderCompletionStatusUpdateRepository orderCompletionStatusUpdateRepository;
    private CustomerRepository customerRepository;
    
    private ProductService productService;
    private EmailService emailService;
    private WhatsappService whatsappService;
    private FCMService fcmService;
    private DeliveryService deliveryService;
    private OrderPostService orderPostService;
    
    private OrderPaymentDetailRepository orderPaymentDetailRepository;
    private String easydukanOrdersEmailAddress;
    private String deliverinOrdersEmailAddress;
     private String easydukanOrdersSenderName;
    private String deliverinOrdersSenderName;
    
    public OrderProcessBulkThread(
            String logprefix, 
            String financeEmailAddress,
            String financeEmailSenderName,
            OrderCompletionStatusUpdate[] bodyOrderCompletionStatusUpdateList,            
            String onboardingOrderLink,
            
            OrderRepository orderRepository,
            StoreDetailsRepository storeDetailsRepository,
            OrderItemRepository orderItemRepository,
            OrderCompletionStatusConfigRepository orderCompletionStatusConfigRepository,
            CartItemRepository cartItemRepository,
            ProductInventoryRepository productInventoryRepository,
            PaymentOrderRepository paymentOrderRepository,
            OrderRefundRepository orderRefundRepository,
            OrderShipmentDetailRepository orderShipmentDetailRepository,
            RegionCountriesRepository regionCountriesRepository,
            OrderPaymentStatusUpdateRepository orderPaymentStatusUpdateRepository,
            OrderCompletionStatusUpdateRepository orderCompletionStatusUpdateRepository,
            OrderPaymentDetailRepository orderPaymentDetailRepository,
            CustomerRepository customerRepository,
            
            ProductService productService,
            EmailService emailService,
            WhatsappService whatsappService,
            FCMService fcmService,
            DeliveryService deliveryService,
            OrderPostService orderPostService,
            String easydukanOrdersEmailAddress,
            String deliverinOrdersEmailAddress
            ) {
        
            this.logprefix = logprefix;
            this.financeEmailAddress = financeEmailAddress;
            this.financeEmailSenderName = financeEmailSenderName;
            this.bodyOrderCompletionStatusUpdateList = bodyOrderCompletionStatusUpdateList;
            this.onboardingOrderLink = onboardingOrderLink;

            this.orderRepository = orderRepository;
            this.storeDetailsRepository = storeDetailsRepository;
            this.orderItemRepository = orderItemRepository;
            this.orderCompletionStatusConfigRepository = orderCompletionStatusConfigRepository;
            this.cartItemRepository = cartItemRepository;
            this.productInventoryRepository = productInventoryRepository;
            this.paymentOrderRepository = paymentOrderRepository;
            this.orderRefundRepository = orderRefundRepository;
            this.orderShipmentDetailRepository = orderShipmentDetailRepository;
            this.regionCountriesRepository = regionCountriesRepository;
            this.orderPaymentStatusUpdateRepository = orderPaymentStatusUpdateRepository;
            this.orderCompletionStatusUpdateRepository = orderCompletionStatusUpdateRepository;
            this.orderPaymentDetailRepository = orderPaymentDetailRepository;
            this.customerRepository = customerRepository;
            
            this.productService = productService;
            this.emailService = emailService;
            this.whatsappService = whatsappService;
            this.fcmService = fcmService;
            this.deliveryService = deliveryService;
            this.orderPostService = orderPostService;
            this.easydukanOrdersEmailAddress = easydukanOrdersEmailAddress;
            this.deliverinOrdersEmailAddress = deliverinOrdersEmailAddress;
    }
        
    public void run(){
        
        List<DeliveryServiceBulkConfirmRequest> bulkConfirmOrderList = new ArrayList();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order count to process:"+this.bodyOrderCompletionStatusUpdateList.length);
        for (int i=0;i<this.bodyOrderCompletionStatusUpdateList.length;i++) {
            
            OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate = bodyOrderCompletionStatusUpdateList[i];
            logprefix = bodyOrderCompletionStatusUpdate.getOrderId();
            
            try {                
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Start process array["+i+"] for orderId:"+bodyOrderCompletionStatusUpdate.getOrderId());
                OrderProcessWorker worker = new OrderProcessWorker(logprefix, 
                        bodyOrderCompletionStatusUpdate.getOrderId(), 
                        financeEmailAddress,
                        financeEmailSenderName,
                        bodyOrderCompletionStatusUpdate,
                        onboardingOrderLink,
                        orderRepository,
                        storeDetailsRepository,
                        orderItemRepository,
                        orderCompletionStatusConfigRepository,
                        cartItemRepository,
                        productInventoryRepository,
                        paymentOrderRepository,
                        orderRefundRepository,
                        orderShipmentDetailRepository,
                        regionCountriesRepository,
                        orderPaymentStatusUpdateRepository,
                        orderCompletionStatusUpdateRepository,
                        customerRepository,
                        
                        productService,
                        emailService,
                        whatsappService,
                        fcmService,
                        deliveryService,
                        orderPostService,
                        false
                        ) ;
                OrderProcessResult result = worker.startProcessOrder();
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "result processOrder orderId:"+bodyOrderCompletionStatusUpdate.getOrderId()+" httpStatus:"+result.httpStatus+" message:"+result.errorMsg+" pendingRequestDelivery:"+result.pendingRequestDelivery);

                if (result.pendingRequestDelivery) {                    
                    //query into from delivery quotation
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "result processOrder orderId:"+bodyOrderCompletionStatusUpdate.getOrderId()+" httpStatus:"+result.httpStatus+" message:"+result.errorMsg+" pendingRequestDelivery:"+result.pendingRequestDelivery);
                    Order orderDetails = (Order)result.data;
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "OrderDetails deliveryType:"+orderDetails.getDeliveryType());
                    Optional<OrderPaymentDetail> optPaymentDetail = orderPaymentDetailRepository.findById(bodyOrderCompletionStatusUpdate.getOrderId());
                    if (optPaymentDetail.isPresent() && !orderDetails.getDeliveryType().equals(DeliveryType.PICKUP)) {
                        DeliveryServiceBulkConfirmRequest bulkConfirmOrder = new DeliveryServiceBulkConfirmRequest();
                        bulkConfirmOrder.deliveryQuotationId=optPaymentDetail.get().getDeliveryQuotationReferenceId();
                        bulkConfirmOrder.orderId=bodyOrderCompletionStatusUpdate.getOrderId();
                        bulkConfirmOrder.previousStatus=result.previousStatus;
                        bulkConfirmOrder.orderCompletionStatusConfig = result.orderCompletionStatusConfig;
                        bulkConfirmOrder.email = result.email;
                        bulkConfirmOrder.storeWithDetails = result.storeWithDetails;
                        bulkConfirmOrderList.add(bulkConfirmOrder);
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "BulkConfirmOrder added deliveryQuotationId:"+bulkConfirmOrder.deliveryQuotationId);
                    } else if (orderDetails.getDeliveryType().equals(DeliveryType.PICKUP)) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DeliveryType is PICKUP");
                    } else {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Payment details not found");
                    }
                }   
            } catch (Exception ex) {
                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Exception occur while processing orderId", ex);
            }
            
        }
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "BulkConfirmOrderList size:"+bulkConfirmOrderList.size());
        if (bulkConfirmOrderList.size()>0) {
            
            //send bulk confirm to delivery-service
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Sending bulkConfirmOrderDelivery() to delivery-service");
            List<DeliveryServiceBulkConfirmResponse> deliveryResponseList = deliveryService.bulkConfirmOrderDelivery(bulkConfirmOrderList);
            if (deliveryResponseList==null) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "deliveryResponseList is null");
                //if null or error, then revert back all deliveryOrder
                for (int i=0;i<bulkConfirmOrderList.size();i++) {
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error while confirming order Delivery. delivery-service return error");
                    
                    Optional<Order> orderOpt = orderRepository.findById(bulkConfirmOrderList.get(i).orderId);
                    Order order = null;
                    if (orderOpt.isPresent()) {
                        order = orderOpt.get();
                    }
                    insertOrderCompletionStatusUpdate(OrderStatus.REQUESTING_DELIVERY_FAILED, "Request delivery fail in bulk process thread", "OrderProcessBulkThread", order.getId());                        
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Revert orderId:"+order.getId()+" to previous status:"+bulkConfirmOrderList.get(i).previousStatus);
                    order.setCompletionStatus(bulkConfirmOrderList.get(i).previousStatus);
                    orderRepository.save(order);
                }
            }
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "deliveryResponseList size:"+deliveryResponseList.size());
            
            for (int i=0;i<deliveryResponseList.size();i++) {
                DeliveryServiceBulkConfirmResponse deliveryResponse = deliveryResponseList.get(i);
                Optional<Order> orderOpt = orderRepository.findById(deliveryResponse.orderId);
                Order order = null;
                if (orderOpt.isPresent()) {
                    order = orderOpt.get();
                }
                
                OrderStatus orderPrevStatus = null;
                OrderCompletionStatusConfig orderCompletionStatusConfig = null;
                Email email = null;
                StoreWithDetails storeWithDetails=null;
                for (int z=0;z<bulkConfirmOrderList.size();z++) {
                    if (deliveryResponse.orderId.equals(bulkConfirmOrderList.get(z).orderId)) {
                        orderPrevStatus = bulkConfirmOrderList.get(z).previousStatus;
                        orderCompletionStatusConfig = bulkConfirmOrderList.get(z).orderCompletionStatusConfig;
                        email = bulkConfirmOrderList.get(z).email;
                        storeWithDetails = bulkConfirmOrderList.get(z).storeWithDetails;
                        break;
                    }
                }
                
                //get order items
                List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
                
                //get the response and update order table        
                if (deliveryResponse.success && order!=null && email!=null && orderPrevStatus!=null) {
                    
                    String orderId = order.getId();
                    OrderStatus status = order.getCompletionStatus();
                    OrderShipmentDetail orderShipmentDetail = orderShipmentDetailRepository.findByOrderId(order.getId());
                    Optional<PaymentOrder> optPaymentDetails = paymentOrderRepository.findByClientTransactionId(order.getId());
                    PaymentOrder paymentDetails = null;
                    if (optPaymentDetails.isPresent()) {
                        paymentDetails = optPaymentDetails.get();
                    }
                    
                    email.getBody().setMerchantTrackingUrl(deliveryResponse.customerTrackingUrl);
                    email.getBody().setCustomerTrackingUrl(deliveryResponse.customerTrackingUrl);
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "delivery confirmed for order: " + orderId + "awaiting for pickup");

                    orderShipmentDetail.setMerchantTrackingUrl(deliveryResponse.customerTrackingUrl);
                    orderShipmentDetail.setCustomerTrackingUrl(deliveryResponse.customerTrackingUrl);
                    orderShipmentDetailRepository.save(orderShipmentDetail);

                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "added tracking urls to orderId:" + orderId);
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "delivery confirmed for order: " + orderId + "awaiting for pickup");
                    
                    //continue other things
                    
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
                                emailContent = MessageGenerator.generateEmailContent(emailContent, order, storeWithDetails, orderItems, orderShipmentDetail, paymentDetails, regionCountry, sendActivationLink, storeWithDetails.getRegionVertical().getCustomerActivationNotice(), customerEmail);
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
                                email.setTo(emailAddress);
                                emailContent = MessageGenerator.generateEmailContent(emailContent, order, storeWithDetails, orderItems, orderShipmentDetail, paymentDetails, regionCountry, false, null, null);
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
                } else {
                    //revert to previous status
                    //find previous status based on orderId                    
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error while confirming order Delivery. deliveryOrder is null ");
                    insertOrderCompletionStatusUpdate(OrderStatus.REQUESTING_DELIVERY_FAILED, "Request delivery fail in bulk process thread", "OrderProcessBulkThread", deliveryResponse.orderId);                        
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Revert orderId:"+deliveryResponse.orderId+" to previous status:"+orderPrevStatus);
                    order.setCompletionStatus(orderPrevStatus);
                    orderRepository.save(order);
                    break;

                }                
                
            } 
            
        }
            
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
