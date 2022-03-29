package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.enums.RefundStatus;
import com.kalsym.order.service.enums.RefundType;
import com.kalsym.order.service.model.Body;
import com.kalsym.order.service.model.DeliveryOrder;
import com.kalsym.order.service.model.Email;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderCompletionStatus;
import com.kalsym.order.service.model.OrderCompletionStatusConfig;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderPaymentStatusUpdate;
import com.kalsym.order.service.model.OrderRefund;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.PaymentOrder;
import com.kalsym.order.service.model.Product;
import com.kalsym.order.service.model.ProductInventory;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.object.OrderProcessResult;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.OrderCompletionStatusConfigRepository;
import com.kalsym.order.service.model.repository.OrderCompletionStatusRepository;
import com.kalsym.order.service.model.repository.OrderCompletionStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.OrderPaymentDetailRepository;
import com.kalsym.order.service.model.repository.OrderPaymentStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderRefundRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.model.repository.OrderShipmentDetailRepository;
import com.kalsym.order.service.model.repository.PaymentOrderRepository;
import com.kalsym.order.service.model.repository.ProductInventoryRepository;
import com.kalsym.order.service.model.repository.RegionCountriesRepository;
import com.kalsym.order.service.model.repository.StoreDetailsRepository;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.service.EmailService;
import com.kalsym.order.service.service.FCMService;
import com.kalsym.order.service.service.OrderPostService;
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.service.WhatsappService;
import com.kalsym.order.service.utility.DateTimeUtil;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.MessageGenerator;
import com.kalsym.order.service.utility.Utilities;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author 7cu
 */
@RestController()
@RequestMapping("/orders/completion-statuses")
public class OrderCompletionStatusController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderCompletionStatusRepository orderCompletionStatusRepository;
   
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
    OrderPaymentDetailRepository orderPaymentDetailRepository;
    
    @Value("${onboarding.order.URL:https://symplified.biz/orders/order-details?orderId=}")
    private String onboardingOrderLink;
    
    @Value("${finance.email.address:finance@symplified.com}")
    private String financeEmailAddress;
    
    @Value("${easydukan.orders.email.address:no-reply@easydukan.co }")
    private String easydukanOrdersEmailAddress;
    
    @Value("${deliverin.orders.email.address:orders@deliverin.my}")
    private String deliverinOrdersEmailAddress;
    
    @GetMapping(path = {""}, name = "order-completion-statuses-get")
    @PreAuthorize("hasAnyAuthority('order-completion-statuses-get', 'all')")
    public ResponseEntity<HttpResponse> getOrderCompletionStatuses(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        String logprefix = request.getRequestURI() + " ";

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderCompletionStatusRepository.findAll(pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "order-completion-statuses-post")
    @PreAuthorize("hasAnyAuthority('order-completion-statuses-post', 'all')")
    public ResponseEntity<HttpResponse> postOrderCompletionStatuses(HttpServletRequest request,
            @Valid @RequestBody OrderCompletionStatus bodyOrderCompletionStatus) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logprefix = request.getRequestURI() + " ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "bodyOrderCompletionStatus.toString(): {}", bodyOrderCompletionStatus.toString());

        Optional<Order> savedOrder = null;

        OrderCompletionStatus orderCompletionStatus;
        try {
            orderCompletionStatus = orderCompletionStatusRepository.save(bodyOrderCompletionStatus);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving orderCompletionStatus", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatus row added");
        response.setData(orderCompletionStatus);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {"/{status}"}, name = "order-completion-statuses-delete-by-status")
    @PreAuthorize("hasAnyAuthority('order-completion-statuses-delete-by-status', 'all')")
    public ResponseEntity<HttpResponse> deleteOrderCompletionStatusesByStatus(HttpServletRequest request,
            @PathVariable(required = true) String status,
            @Valid @RequestBody OrderCompletionStatus bodyOrderCompletionStatus) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-completion-statuses-delete-by-status, status: {}", status);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderCompletionStatus.toString(), "");

        try {
            orderCompletionStatusRepository.deleteById(status);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error deleting orderCompletionStatus", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatus deleted with status: {}", bodyOrderCompletionStatus.getStatus());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(path = {"/{status}"}, name = "order-completion-statuses-put-by-status")
    @PreAuthorize("hasAnyAuthority('order-completion-statuses-put-by-status', 'all')")
    public ResponseEntity<HttpResponse> putOrderCompletionStatusesByStatus(HttpServletRequest request,
            @PathVariable(required = true) String status,
            @Valid @RequestBody OrderCompletionStatus bodyOrderCompletionStatus) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-completion-statuses-put-by-status, status: {}", status);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "bodyOrderCompletionStatus.toString(): {}", bodyOrderCompletionStatus.toString());

        Optional<OrderCompletionStatus> optOrderCompletionStatus = orderCompletionStatusRepository.findById(status);

        if (!optOrderCompletionStatus.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatus not found with status: {}", status);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatus found with status: {}", status);
        OrderCompletionStatus orderCompletionStatus = optOrderCompletionStatus.get();

        orderCompletionStatus.update(bodyOrderCompletionStatus);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatus updated for status: {}", status);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderCompletionStatusRepository.save(orderCompletionStatus));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    
    @PutMapping(path = {"/bulk"}, name = "order-completion-status-updates-put-by-bulk")
    @PreAuthorize("hasAnyAuthority('order-completion-status-updates-put-by-bulk', 'all')")
    public ResponseEntity<HttpResponse> putOrderCompletionStatusUpdatesBulk(HttpServletRequest request,
          @Valid @RequestBody OrderCompletionStatusUpdate[] bodyOrderCompletionStatusUpdateList) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-completion-status-updates-confirm-put-by-bulk");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Total order sent:"+bodyOrderCompletionStatusUpdateList.length);
        
        if (bodyOrderCompletionStatusUpdateList.length>10) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order count more than 10");
            response.setMessage("Cannot process order more than 10");
            response.setErrorStatus(HttpStatus.CONFLICT);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        
        for (int i=0;i<bodyOrderCompletionStatusUpdateList.length;i++) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Id:"+bodyOrderCompletionStatusUpdateList[i].getId()+" OrderId:"+bodyOrderCompletionStatusUpdateList[i].getOrderId());
        }
        
        OrderProcessBulkThread processThread = new OrderProcessBulkThread(logprefix,                  
              financeEmailAddress,
              bodyOrderCompletionStatusUpdateList,

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
              orderPaymentDetailRepository,
                
              productService,
              emailService,
              whatsappService,
              fcmService,
              deliveryService,
              orderPostService,
              easydukanOrdersEmailAddress,
              deliverinOrdersEmailAddress) ;
        processThread.start();

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "OrderProcessThread started");

        //return ack, not real response
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

    }
    
}
