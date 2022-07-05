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
@RequestMapping("/ordergroup/{orderGroupId}/completion-status-updates")
public class OrderGroupStatusUpdateController {

    @Autowired
    OrderRepository orderRepository;
    
    @Autowired
    OrderGroupRepository orderGroupRepository;
    
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
    
    @Autowired
    CustomerRepository customerRepository;
    
    @Autowired
    VoucherRepository voucherRepository;
    
    @Autowired
    CustomerVoucherRepository customerVoucherRepository;
    
    @Value("${onboarding.order.URL:https://symplified.biz/orders/order-details?orderId=}")
    private String onboardingOrderLink;
    
    @Value("${finance.email.address:orders@deliverin.my}")
    private String financeEmailAddress; 
    
    @Value("${finance.email.sender.name:Deliver In Orders}")
    private String financeEmailSenderName;
    
    @Value("${order.invoice.base.URL:https://api.symplified.it/orders/pdf/}")
    private String orderInvoiceBaseUrl;
    
    @Value("${asset.service.URL:https://assets.symplified.it}")
    private String assetServiceBaseUrl;
    
    @PutMapping(path = {""}, name = "order-completion-status-updates-put-by-order-group-id")
    @PreAuthorize("hasAnyAuthority('order-completion-status-updates-put-by-order-group-id', 'all')")
    public ResponseEntity<HttpResponse> putOrderGroupCompletionStatusUpdate(HttpServletRequest request,
            @PathVariable(required = true) String orderGroupId,
            @Valid @RequestBody OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        //remove prefix G
        orderGroupId = orderGroupId.substring(1);
        
        Optional<OrderGroup> orderGroupOpt = orderGroupRepository.findById(orderGroupId);
        if (!orderGroupOpt.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "OrderGroup not found with orderGroupId: " + orderGroupId);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("Order Group not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        
        OrderGroup orderGroup = orderGroupOpt.get();
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order found for this groupId:"+orderGroup.getOrderList().size());
        
        //find all order for the same group Id
        for (int i=0;i<orderGroup.getOrderList().size();i++) {
            String orderId = orderGroup.getOrderList().get(i).getId();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Start process orderId:"+orderId);
            OrderProcessWorker worker = new OrderProcessWorker(logprefix, 
                orderId, 
                financeEmailAddress,
                financeEmailSenderName,
                bodyOrderCompletionStatusUpdate,
                onboardingOrderLink,
                orderInvoiceBaseUrl,

                orderRepository,
                orderGroupRepository,
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
                voucherRepository,
                customerVoucherRepository,

                productService,
                emailService,
                whatsappService,
                fcmService,
                deliveryService,
                orderPostService,
                true,
                assetServiceBaseUrl) ;
            OrderProcessResult result = worker.startProcessOrder();        
        }
                
        response.setStatus(HttpStatus.OK.value());        
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    

}
