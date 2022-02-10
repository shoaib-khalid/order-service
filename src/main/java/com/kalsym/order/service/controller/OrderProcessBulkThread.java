/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
import com.kalsym.order.service.model.OrderPaymentDetail;
import com.kalsym.order.service.model.object.OrderProcessResult;
import com.kalsym.order.service.model.object.DeliveryServiceBulkConfirmRequest;
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
import com.kalsym.order.service.utility.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 *
 * @author taufik
 */
public class OrderProcessBulkThread extends Thread {
    
    private final String logprefix;
    private final String financeEmailAddress;
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
    
    private ProductService productService;
    private EmailService emailService;
    private WhatsappService whatsappService;
    private FCMService fcmService;
    private DeliveryService deliveryService;
    private OrderPostService orderPostService;
    
    private OrderPaymentDetailRepository orderPaymentDetailRepository;
    
    public OrderProcessBulkThread(
            String logprefix, 
            String financeEmailAddress,
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
            
            ProductService productService,
            EmailService emailService,
            WhatsappService whatsappService,
            FCMService fcmService,
            DeliveryService deliveryService,
            OrderPostService orderPostService
            ) {
        
            this.logprefix = logprefix;
            this.financeEmailAddress = financeEmailAddress;
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

            this.productService = productService;
            this.emailService = emailService;
            this.whatsappService = whatsappService;
            this.fcmService = fcmService;
            this.deliveryService = deliveryService;
            this.orderPostService = orderPostService;
    }
        
    public void run(){
        
        List<DeliveryServiceBulkConfirmRequest> bulkConfirmOrderList = new ArrayList();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order count to process:"+this.bodyOrderCompletionStatusUpdateList.length);
        for (int i=0;i<this.bodyOrderCompletionStatusUpdateList.length;i++) {
            OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate = bodyOrderCompletionStatusUpdateList[i];
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Start process for orderId:"+bodyOrderCompletionStatusUpdate.getOrderId());
            OrderProcessWorker worker = new OrderProcessWorker(logprefix, 
                    bodyOrderCompletionStatusUpdate.getOrderId(), 
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
                    false) ;
            OrderProcessResult result = worker.startProcessOrder();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "result processOrder orderId:"+bodyOrderCompletionStatusUpdate.getOrderId()+" httpStatus:"+result.httpStatus+" message:"+result.errorMsg+" pendingRequestDelivery:"+result.pendingRequestDelivery);
                        
            if (result.pendingRequestDelivery) {
                //query into from delivery quotation
                Optional<OrderPaymentDetail> optPaymentDetail = orderPaymentDetailRepository.findById(bodyOrderCompletionStatusUpdate.getOrderId());
                if (optPaymentDetail.isPresent()) {
                    DeliveryServiceBulkConfirmRequest bulkConfirmOrder = new DeliveryServiceBulkConfirmRequest();
                    bulkConfirmOrder.deliveryQuotationId=optPaymentDetail.get().getDeliveryQuotationReferenceId();
                    bulkConfirmOrder.orderId=bodyOrderCompletionStatusUpdate.getOrderId();
                    bulkConfirmOrderList.add(bulkConfirmOrder);
                }
            }                        
            
        }
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "BulkConfirmOrderList size:"+bulkConfirmOrderList.size());
        if (bulkConfirmOrderList.size()>0) {
            //send bulk confirm to delivery-service
            deliveryService.bulkConfirmOrderDelivery(bulkConfirmOrderList);
            //TODO: get the response and update order table
        }
            
    }
    
}
