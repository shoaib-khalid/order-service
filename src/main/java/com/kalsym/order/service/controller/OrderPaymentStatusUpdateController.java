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
import com.kalsym.order.service.service.WhatsappService;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.MessageGenerator;
import com.kalsym.order.service.utility.Utilities;
import com.kalsym.order.service.utility.DateTimeUtil;

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
import com.kalsym.order.service.model.object.OrderProcessResult;
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
    WhatsappService whatsappService;

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
    
    @Value("${easydukan.orders.email.address:no-reply@easydukan.co }")
    private String easydukanOrdersEmailAddress;
    
    @Value("${easydukan.orders.sender.name:Easy Dukan }")
    private String easydukanOrdersSenderName;
    
    @Value("${deliverin.orders.email.address:orders@deliverin.my}")
    private String deliverinOrdersEmailAddress;
    
    @Value("${deliverin.orders.sender.name:Deliver In Orders}")
    private String deliverinOrdersSenderName;

    @PutMapping(path = {""}, name = "order-completion-status-updates-put-by-order-id")
    @PreAuthorize("hasAnyAuthority('order-completion-status-updates-put-by-order-id', 'all')")
    public ResponseEntity<HttpResponse> putOrderCompletionStatusUpdatesConfirm(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @Valid @RequestBody OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        OrderProcessWorker worker = new OrderProcessWorker(logprefix, 
            orderId, 
            financeEmailAddress,
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
            
            productService,
            emailService,
            whatsappService,
            fcmService,
            deliveryService,
            orderPostService,
            true,
            easydukanOrdersEmailAddress,
            deliverinOrdersEmailAddress,
            easydukanOrdersSenderName,
            deliverinOrdersSenderName) ;
        OrderProcessResult result = worker.startProcessOrder();
        response.setData(result.data);
        response.setSuccessStatus(result.httpStatus);
        response.setMessage(result.errorMsg);
        return ResponseEntity.status(result.httpStatus).body(response);
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

            DeliveryResponse deliveryResponse = deliveryService.confirmOrderDelivery(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId(), order.getId(), bodyOrderCompletionStatusUpdate.getPickupDate(), bodyOrderCompletionStatusUpdate.getPickupTime());
            DeliveryOrder deliveryOrder = (DeliveryOrder)deliveryResponse.getOrderCreated();
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

    

}
