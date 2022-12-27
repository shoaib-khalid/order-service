package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.enums.ProductStatus;
import com.kalsym.order.service.enums.StorePaymentType;
import com.kalsym.order.service.enums.DeliveryType;
import com.kalsym.order.service.enums.DiscountCalculationType;
import com.kalsym.order.service.enums.DiscountType;
import com.kalsym.order.service.enums.RefundType;
import com.kalsym.order.service.enums.RefundStatus;
import com.kalsym.order.service.enums.VehicleType;
import com.kalsym.order.service.enums.CartStage;
import com.kalsym.order.service.enums.ServiceType;
import com.kalsym.order.service.enums.DineInOption;
import com.kalsym.order.service.enums.Channel;
import com.kalsym.order.service.model.Body;
import com.kalsym.order.service.model.OrderPaymentDetail;
import com.kalsym.order.service.model.object.CustomPageable;
import javax.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import com.kalsym.order.service.utility.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import com.kalsym.order.service.service.EmailService;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderWithDetails;
import com.kalsym.order.service.model.COD;
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.CartSubItem;
import com.kalsym.order.service.model.ProductInventory;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.StoreDeliveryDetail;
import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.CartItemAddOn;
import com.kalsym.order.service.model.CustomerVoucher;
import com.kalsym.order.service.model.Customer;
import com.kalsym.order.service.model.DeliveryOrder;
import com.kalsym.order.service.model.DeliveryQuotation;
import com.kalsym.order.service.model.Email;
import com.kalsym.order.service.model.OrderCompletionStatusConfig;
import com.kalsym.order.service.model.Product;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
import com.kalsym.order.service.model.OrderPaymentStatusUpdate;
import com.kalsym.order.service.model.Store;
import com.kalsym.order.service.model.StoreCommission;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderSubItem;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.OrderRefund;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderGroup;
import com.kalsym.order.service.model.ProductInventoryItem;
import com.kalsym.order.service.model.ProductVariantAvailable;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.PaymentOrder;
import com.kalsym.order.service.model.StoreDiscount;
import com.kalsym.order.service.model.Voucher;
import com.kalsym.order.service.model.VoucherStore;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.OrderPaymentDetail;
import com.kalsym.order.service.model.ProductAddOn;
import com.kalsym.order.service.model.TagConfig;
import com.kalsym.order.service.model.TagDetails;
import com.kalsym.order.service.model.TagKeyword;
import com.kalsym.order.service.model.TagProductFeature;
import com.kalsym.order.service.model.object.Discount;
import com.kalsym.order.service.model.object.OrderObject;
import com.kalsym.order.service.model.object.OrderDetails;
import com.kalsym.order.service.model.object.ItemDiscount;
import com.kalsym.order.service.model.QrcodeOrderGroup;
import com.kalsym.order.service.model.object.OrderGroupObject;
import com.kalsym.order.service.model.repository.CartItemAddOnRepository;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.OrderSubItemRepository;
import com.kalsym.order.service.model.repository.OrderItemAddOnRepository;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.CartRepository;
import com.kalsym.order.service.model.repository.CartSubItemRepository;
import com.kalsym.order.service.model.repository.OrderCompletionStatusConfigRepository;
import com.kalsym.order.service.model.repository.OrderCompletionStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderPaymentDetailRepository;
import com.kalsym.order.service.model.repository.OrderPaymentStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.model.repository.OrderWithDetailsRepository;
import com.kalsym.order.service.model.repository.OrderRefundRepository;
import com.kalsym.order.service.model.repository.ProductRepository;
import com.kalsym.order.service.model.repository.StoreRepository;
import com.kalsym.order.service.model.repository.OrderShipmentDetailRepository;
import com.kalsym.order.service.model.repository.ProductInventoryRepository;
import com.kalsym.order.service.model.repository.RegionCountriesRepository;
import com.kalsym.order.service.model.repository.StoreDetailsRepository;
import com.kalsym.order.service.model.repository.StoreDiscountRepository;
import com.kalsym.order.service.model.repository.StoreDiscountTierRepository;
import com.kalsym.order.service.model.repository.StoreDeliveryDetailRepository;
import com.kalsym.order.service.model.repository.PaymentOrderRepository;
import com.kalsym.order.service.model.repository.CustomerVoucherRepository;
import com.kalsym.order.service.model.repository.VoucherRepository;
import com.kalsym.order.service.model.repository.CustomerRepository;
import com.kalsym.order.service.model.repository.OrderGroupRepository;
import com.kalsym.order.service.model.repository.ProductAddOnRepository;
import com.kalsym.order.service.model.repository.TagRepository;
import com.kalsym.order.service.model.repository.TagProductFeatureRepository;
import com.kalsym.order.service.service.CustomerService;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.service.FCMService;
import com.kalsym.order.service.service.OrderPostService;
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.service.WhatsappService;
import com.kalsym.order.service.utility.DateTimeUtil;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.MessageGenerator;
import com.kalsym.order.service.utility.OrderCalculation;
import static com.kalsym.order.service.utility.OrderCalculation.calculateStoreServiceCharges;
import com.kalsym.order.service.utility.OrderWorker;
import com.kalsym.order.service.utility.StoreDiscountCalculation;
import com.kalsym.order.service.utility.TxIdUtil;
import com.kalsym.order.service.utility.Utilities;
import com.kalsym.order.service.utility.GeneratePdfReport;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.validation.Valid;
import java.util.Optional;
import javax.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;
import java.io.ByteArrayInputStream;
import org.springframework.http.HttpHeaders;
import com.kalsym.order.service.model.repository.QrcodeOrderGroupRepository;

/**
 *
 *
 * @author 7cu
 */
@RestController()
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    CustomerService customerService;

    @Autowired
    OrderRepository orderRepository;
    
    @Autowired
    OrderGroupRepository orderGroupRepository;
    
    @Autowired
    OrderWithDetailsRepository orderWithDetailsRepository;

    @Autowired
    OrderPostService orderPostService;

    @Autowired
    DeliveryService deliveryService;

    @Autowired
    ProductService productService;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    EmailService emailService;
    
    @Autowired
    FCMService fcmService;
    
    @Autowired
    WhatsappService whatsappService;
    
    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderSubItemRepository orderSubItemRepository;
    
    @Autowired
    OrderItemAddOnRepository orderItemAddOnRepository;
    
    @Autowired
    CartItemRepository cartItemRepository;
    
    @Autowired
    CartSubItemRepository cartSubItemRepository;

    @Autowired
    OrderShipmentDetailRepository orderShipmentDetailRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    OrderPaymentDetailRepository orderPaymentDetailRepository;

    @Autowired
    OrderCompletionStatusUpdateRepository orderCompletionStatusUpdateRepository;

    @Autowired
    OrderPaymentStatusUpdateRepository orderPaymentStatusUpdateRepository;
    
    @Autowired
    ProductInventoryRepository productInventoryRepository;
    
    @Autowired
    StoreDiscountRepository storeDiscountRepository;
    
    @Autowired
    StoreDiscountTierRepository storeDiscountTierRepository;
    
    @Autowired
    OrderCompletionStatusConfigRepository orderCompletionStatusConfigRepository;
    
    @Autowired
    RegionCountriesRepository regionCountriesRepository;
    
    @Autowired
    StoreDetailsRepository storeDetailsRepository;
    
    @Autowired
    StoreDeliveryDetailRepository storeDeliveryDetailRepository;
    
    @Autowired
    OrderRefundRepository orderRefundRepository;
    
    @Autowired
    PaymentOrderRepository paymentOrderRepository;
    
    @Autowired
    CustomerVoucherRepository customerVoucherRepository;
    
    @Autowired
    VoucherRepository voucherRepository;
    
    @Autowired
    CustomerRepository customerRepository;
    
    @Autowired
    TagRepository tagRepository;
    
    @Autowired
    TagProductFeatureRepository tagProductFeatureRepository;
    
    @Autowired
    QrcodeOrderGroupRepository qrcodeOrderGroupRepository;
    
    @Autowired
    ProductAddOnRepository productAddOnRepository;
    
    @Autowired
    CartItemAddOnRepository cartItemAddOnRepository;
    
    @Value("${onboarding.order.URL:https://symplified.biz/orders/order-details?orderId=}")
    private String onboardingOrderLink;
    
    @Value("${order.invoice.base.URL:https://api.symplified.it/order-service/v1/orders/pdf/}")
    private String orderInvoiceBaseUrl;
    
    @Value("${asset.service.URL:https://assets.symplified.it}")
    private String assetServiceBaseUrl;
    
    @Value("${whatsapp.process.order.URL:https://api.symplified.it/order-service/v1/orders/%orderId%/completion-status-updates}")
    private String processOrderUrl;
    
    //@PreAuthorize("hasAnyAuthority('orders-get', 'all') and (@customOwnerVerifier.VerifyStore(#storeId) or @customOwnerVerifier.VerifyCustomer(#customerId))")    
    @GetMapping(path = {""}, name = "orders-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get', 'all')")
    public ResponseEntity<HttpResponse> getOrders(HttpServletRequest request,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
            @RequestParam(required = false) String invoiceId,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) String deliveryQuotationReferenceId,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String zipcode,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) OrderStatus completionStatus,
            @RequestParam(required = false) ServiceType serviceType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        String logprefix = request.getRequestURI() + " getOrders() ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "before from : " + from + ", to : " + to);
//        to.setDate(to.getDate() + 1);
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "after adding 1 day to (todate) from : " + from + ", to : " + to);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orders-get request " + request.getRequestURL());
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Order orderMatch = new Order();
        if (storeId != null && !storeId.isEmpty()) {
            orderMatch.setStoreId(storeId);
        }
        if (customerId != null && !customerId.isEmpty()) {
            orderMatch.setCustomerId(customerId);
        }

        if (paymentStatus != null) {
            orderMatch.setPaymentStatus(paymentStatus);
        }

        if (invoiceId != null && !invoiceId.isEmpty()) {
            orderMatch.setInvoiceId(invoiceId);
        }
        
        if (serviceType != null) {
            orderMatch.setServiceType(serviceType);
        }

        /*if (completionStatus != null) {            
            orderMatch.setCompletionStatus(completionStatus);            
        }*/
        
        Store storeDetail = new Store();
        if (clientId != null && !clientId.isEmpty()) {
            storeDetail.setClientId(clientId);
        }
        
        orderMatch.setStore(storeDetail);        
        
        
        OrderPaymentDetail opd = new OrderPaymentDetail();
        if (accountName != null && !accountName.isEmpty()) {
            opd.setAccountName(accountName);
        }

        if (deliveryQuotationReferenceId != null && !deliveryQuotationReferenceId.isEmpty()) {
            opd.setDeliveryQuotationReferenceId(deliveryQuotationReferenceId);
        }

        orderMatch.setOrderPaymentDetail(opd);

        OrderShipmentDetail osd = new OrderShipmentDetail();
        if (receiverName != null && !receiverName.isEmpty()) {
            osd.setReceiverName(receiverName);
        }

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            osd.setPhoneNumber(phoneNumber);
        }

        if (city != null && !city.isEmpty()) {
            osd.setCity(city);
        }

        if (zipcode != null && !zipcode.isEmpty()) {
            osd.setZipcode(zipcode);
        }

        orderMatch.setOrderShipmentDetail(osd);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderMatch: " + orderMatch);
        
        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Order> orderExample = Example.of(orderMatch, matcher);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("created").descending());

        Page<Order> orderWithPage = orderRepository.findAll(getSpecWithDatesBetween(from, to, completionStatus, orderExample), pageable);
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderWithPage);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @GetMapping(path = {"/details"}, name = "orders-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get', 'all')")
    public ResponseEntity<HttpResponse> getOrdersWithDetails(HttpServletRequest request,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
            @RequestParam(required = false) String invoiceId,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) String deliveryQuotationReferenceId,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String zipcode,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) OrderStatus[] completionStatus,
            @RequestParam(required = false) String[] orderIds,
            @RequestParam(required = false) String[] orderGroupIds,
            @RequestParam(required = false) ServiceType serviceType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        String logprefix = request.getRequestURI() + " getOrdersWithDetails() ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "before from : " + from + ", to : " + to);
//        to.setDate(to.getDate() + 1);
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "after adding 1 day to (todate) from : " + from + ", to : " + to);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orders-get request " + request.getRequestURL());
        HttpResponse response = new HttpResponse(request.getRequestURI());

        OrderWithDetails orderMatch = new OrderWithDetails();
        if (storeId != null && !storeId.isEmpty()) {
            orderMatch.setStoreId(storeId);
        }
        if (customerId != null && !customerId.isEmpty()) {
            orderMatch.setCustomerId(customerId);
        }

        if (paymentStatus != null) {
            orderMatch.setPaymentStatus(paymentStatus);
        }

        if (invoiceId != null && !invoiceId.isEmpty()) {
            orderMatch.setInvoiceId(invoiceId);
        }
        
        if (serviceType != null) {
            orderMatch.setServiceType(serviceType);
        }

        /*if (completionStatus != null) {            
            orderMatch.setCompletionStatus(completionStatus);            
        }*/
        
        StoreWithDetails storeDetail = new StoreWithDetails();
        if (clientId != null && !clientId.isEmpty()) {
            storeDetail.setClientId(clientId);
        }
        
        orderMatch.setStore(storeDetail);        
        
        
        OrderPaymentDetail opd = new OrderPaymentDetail();
        if (accountName != null && !accountName.isEmpty()) {
            opd.setAccountName(accountName);
        }

        if (deliveryQuotationReferenceId != null && !deliveryQuotationReferenceId.isEmpty()) {
            opd.setDeliveryQuotationReferenceId(deliveryQuotationReferenceId);
        }

        orderMatch.setOrderPaymentDetail(opd);

        OrderShipmentDetail osd = new OrderShipmentDetail();
        if (receiverName != null && !receiverName.isEmpty()) {
            osd.setReceiverName(receiverName);
        }

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            osd.setPhoneNumber(phoneNumber);
        }

        if (city != null && !city.isEmpty()) {
            osd.setCity(city);
        }

        if (zipcode != null && !zipcode.isEmpty()) {
            osd.setZipcode(zipcode);
        }

        orderMatch.setOrderShipmentDetail(osd);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderMatch: " + orderMatch);
        
        
        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<OrderWithDetails> orderExample = Example.of(orderMatch, matcher);
        
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("created").descending());
        
        Page<OrderWithDetails> orderWithPage = orderWithDetailsRepository.findAll(getOrderWithDetailsSpecWithDatesBetweenMultipleStatus(from, to, completionStatus, orderIds, orderGroupIds, orderExample), pageable);
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderWithPage);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }    
    
    @GetMapping(path = {"/search"}, name = "orders-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get', 'all')")
    public ResponseEntity<HttpResponse> searchOrderDetails(HttpServletRequest request,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
            @RequestParam(required = false) String invoiceId,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) String deliveryQuotationReferenceId,
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String zipcode,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) ServiceType serviceType,
            @RequestParam(required = false) OrderStatus[] completionStatus,
            @RequestParam(required = false, defaultValue = "created") String sortByCol,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortingOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        String logprefix = request.getRequestURI() + " searchOrderDetails() ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "before from : " + from + ", to : " + to);
//        to.setDate(to.getDate() + 1);
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "after adding 1 day to (todate) from : " + from + ", to : " + to);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orders-get request " + request.getRequestURL());
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Order orderMatch = new Order();
        if (storeId != null && !storeId.isEmpty()) {
            orderMatch.setStoreId(storeId);
        }
        if (customerId != null && !customerId.isEmpty()) {
            orderMatch.setCustomerId(customerId);
        }

        if (paymentStatus != null) {
            orderMatch.setPaymentStatus(paymentStatus);
        }

        if (invoiceId != null && !invoiceId.isEmpty()) {
            orderMatch.setInvoiceId(invoiceId);
        }

        if (serviceType != null) {
            orderMatch.setServiceType(serviceType);
        }
        
        /*if (completionStatus != null) {            
            orderMatch.setCompletionStatus(completionStatus);            
        }*/
        
        Store storeDetail = new Store();
        if (clientId != null && !clientId.isEmpty()) {
            storeDetail.setClientId(clientId);
        }
        
        orderMatch.setStore(storeDetail);        
        
        
        OrderPaymentDetail opd = new OrderPaymentDetail();
        if (accountName != null && !accountName.isEmpty()) {
            opd.setAccountName(accountName);
        }

        if (deliveryQuotationReferenceId != null && !deliveryQuotationReferenceId.isEmpty()) {
            opd.setDeliveryQuotationReferenceId(deliveryQuotationReferenceId);
        }

        orderMatch.setOrderPaymentDetail(opd);

        OrderShipmentDetail osd = new OrderShipmentDetail();
        if (receiverName != null && !receiverName.isEmpty()) {
            osd.setReceiverName(receiverName);
        }

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            osd.setPhoneNumber(phoneNumber);
        }

        if (city != null && !city.isEmpty()) {
            osd.setCity(city);
        }

        if (zipcode != null && !zipcode.isEmpty()) {
            osd.setZipcode(zipcode);
        }

        orderMatch.setOrderShipmentDetail(osd);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderMatch: " + orderMatch);
        
        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Order> orderExample = Example.of(orderMatch, matcher);
        
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("created").descending());
        if (sortingOrder==Sort.Direction.ASC)
            pageable = PageRequest.of(page, pageSize, Sort.by(sortByCol).ascending());
        else if (sortingOrder==Sort.Direction.DESC)
            pageable = PageRequest.of(page, pageSize, Sort.by(sortByCol).descending());
        
        Page<Order> orderWithPage = orderRepository.findAll(getSpecWithDatesBetweenMultipleStatus(from, to, completionStatus, orderExample), pageable);
        List<Order> orderList = orderWithPage.getContent();
        
        OrderDetails[] orderDetailsList = new OrderDetails[orderList.size()];
        for (int i=0;i<orderList.size();i++) {
            Order order = orderList.get(i);
            OrderDetails orderDetails = new OrderDetails();
            orderDetails.setOrder(order);            
            orderDetails.setCurrentCompletionStatus(order.getCompletionStatus().name());
        
            Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(order.getStoreId());
            StoreWithDetails storeWithDetails = optStore.get();
            String verticalId = storeWithDetails.getVerticalCode();
            Boolean storePickup = order.getOrderShipmentDetail().getStorePickup();
            String storeDeliveryType = storeWithDetails.getStoreDeliveryDetail().getType();
            if (order.getServiceType()!=null && order.getServiceType()==ServiceType.DINEIN) {
                storeDeliveryType = order.getDineInOption().name();
            }
                        
            //get current status
            String currentStatus = order.getCompletionStatus().name();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Find config for current status VerticalId:"+verticalId+" Status:"+currentStatus+" storePickup:"+storePickup+" storeDeliveryType:"+storeDeliveryType+" paymentType:"+order.getPaymentType()+" dineInOption:"+order.getDineInOption());
            List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = null;
            orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, currentStatus, storePickup, storeDeliveryType, order.getPaymentType());            
            OrderCompletionStatusConfig orderCompletionStatusConfig = null;
            if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for current status: " + currentStatus);            
            } else {        
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size());
                orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);

                //get next action
                OrderCompletionStatusConfig nextCompletionStatusConfig = null;
                int nextSequence = orderCompletionStatusConfig.getStatusSequence()+1;
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Find config for next status VerticalId:"+verticalId+" NextSequence:"+nextSequence+" storePickup:"+storePickup+" storeDeliveryType:"+storeDeliveryType+" paymentType:"+order.getPaymentType()+" dineInOption:"+order.getDineInOption());
                
                List<OrderCompletionStatusConfig> nextActionCompletionStatusConfigs = null;
                nextActionCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusSequenceAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, nextSequence, storePickup, storeDeliveryType, order.getPaymentType());                    
               
                if (nextActionCompletionStatusConfigs == null || nextActionCompletionStatusConfigs.isEmpty()) {
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for next sequence:"+nextSequence);
                } else {
                    nextCompletionStatusConfig = nextActionCompletionStatusConfigs.get(0);
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Next action status: " + nextCompletionStatusConfig.getStatus()+" sequence:"+nextCompletionStatusConfig.getStatusSequence());
                    orderDetails.setNextCompletionStatus(nextCompletionStatusConfig.status);
                    orderDetails.setNextActionText(nextCompletionStatusConfig.nextActionText);
                }
            }
                    
            orderDetailsList[i] = orderDetails;
        }
        
        //create custom pageable object with modified content
        CustomPageable customPageable = new CustomPageable();
        customPageable.content = orderDetailsList;
        customPageable.pageable = orderWithPage.getPageable();
        customPageable.totalPages = orderWithPage.getTotalPages();
        customPageable.totalElements = orderWithPage.getTotalElements();
        customPageable.last = orderWithPage.isLast();
        customPageable.size = orderWithPage.getSize();
        customPageable.number = orderWithPage.getNumber();
        customPageable.sort = orderWithPage.getSort();        
        customPageable.numberOfElements = orderWithPage.getNumberOfElements();
        customPageable.first  = orderWithPage.isFirst();
        customPageable.empty = orderWithPage.isEmpty();
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(customPageable);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    
    @GetMapping(path = {"/{id}"}, name = "orders-get-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get-by-id', 'all')")
    public ResponseEntity<HttpResponse> getOrdersById(HttpServletRequest request,
            @PathVariable(required = true) String id) {

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Optional<OrderWithDetails> optOrder = orderWithDetailsRepository.findById(id);
        if (!optOrder.isPresent()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("order with id " + id + " not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        //get order group details
        OrderWithDetails order = optOrder.get();
        if (order.getOrderGroupId()!=null) {
            Optional<OrderGroup> orderGroupOpt = orderGroupRepository.findById(order.getOrderGroupId());
            if (orderGroupOpt.isPresent()) {
                OrderGroupObject orderGroup = new OrderGroupObject();
                orderGroup.setTotal(orderGroupOpt.get().getTotal());    
                orderGroup.setPlatformVoucherId(orderGroupOpt.get().getPlatformVoucherId());
                orderGroup.setPlatformVoucherDiscount(orderGroupOpt.get().getPlatformVoucherDiscount());
                order.setOrderGroupDetails(orderGroup);
            }
        }
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(order);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @GetMapping(path = {"/group/{groupId}"}, name = "orders-get-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get-by-id', 'all')")
    public ResponseEntity<HttpResponse> getOrderGroupById(HttpServletRequest request,
            @PathVariable(required = true) String groupId) {

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Optional<OrderGroup> optOrderGroup = orderGroupRepository.findById(groupId);
        if (!optOrderGroup.isPresent()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("Order with group id " + groupId + " not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(optOrderGroup.get());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }    
    
    /**
     *
     *
     * @param request
     * @param cartId     
     * @param saveCustomerInformation
     * @param platformVoucherCode
     * @param sendReceiptToReceiver
     * @param storeId
     * @param channel     
     * @param cod
     * @param qrToken
     * @param tableNo
     * @return
     * @throws Exception
     */
    @PostMapping(path = {"/placeOrder"}, name = "orders-push-cod")
    @PreAuthorize("hasAnyAuthority('orders-push-cod', 'all')")
    public ResponseEntity<HttpResponse> placeOrder(HttpServletRequest request,
            @RequestParam(required = true) String cartId, 
            @RequestParam(required = false) Boolean saveCustomerInformation,
            @RequestParam(required = false) String platformVoucherCode,
            @RequestParam(required = false) Boolean sendReceiptToReceiver,
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) Channel channel,
            @RequestParam(required = false) String qrToken,
            @RequestParam(required = false) String tableNo,                        
            @RequestBody COD cod) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orders-push-cod request on url: " + request.getRequestURI());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orders-push-cod request body: " + cod.toString());        
        
        HttpResponse response = new HttpResponse(request.getRequestURI());        
            
        Optional<Cart> optCart = cartRepository.findById(cartId);
        if (!optCart.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart with id " + cartId + " not found");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("Cart with id " + cartId + " not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        
        //update cart status
        Cart cart = optCart.get();
        cart.setStage(CartStage.ORDER_PLACED);
        cartRepository.save(cart);
        
        // get cart items 
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got cartItems of cartId: " + cartId + ", items: " + cartItems.toString());

        //if cart empty
        if (cartItems.isEmpty()) {
            response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
            response.setMessage("Cart is empty");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
            
        //check platform voucher code if provided
        CustomerVoucher customerPlatformVoucher = null;
        if (platformVoucherCode!=null && !"".equals(platformVoucherCode)) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "PlatformVoucherCode provided : "+platformVoucherCode);
            customerPlatformVoucher = customerVoucherRepository.findCustomerPlatformVoucherByCode(cod.getCustomerId(), platformVoucherCode, new Date());
            if (customerPlatformVoucher==null) {                
                //find guest voucher
                Voucher guestVoucher = customerVoucherRepository.findGuestPlatformVoucherByCode(platformVoucherCode, new Date());
                if (guestVoucher!=null) {
                    //check if already redeem
                    List<CustomerVoucher> usedVoucherList = customerVoucherRepository.findByGuestEmailAndVoucherId(cod.getOrderShipmentDetails().getEmail(), guestVoucher.getId());
                    if (usedVoucherList.size()>0) {  
                        CustomerVoucher usedVoucher = usedVoucherList.get(0);
                        if (usedVoucher.getIsUsed() && !guestVoucher.getAllowMultipleRedeem()) {
                            //already used
                            response.setStatus(HttpStatus.NOT_FOUND.value());
                            response.setMessage("Voucher code " + platformVoucherCode + " already used");
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } 
                        customerPlatformVoucher = usedVoucher;
                    } else {
                        customerPlatformVoucher = new CustomerVoucher();
                        customerPlatformVoucher.setCreated(new Date());
                    }
                    customerPlatformVoucher.setGuestEmail(cod.getOrderShipmentDetails().getEmail());
                    customerPlatformVoucher.setIsUsed(false);
                    customerPlatformVoucher.setVoucherId(guestVoucher.getId());
                    customerPlatformVoucher.setVoucher(guestVoucher);
                    customerPlatformVoucher.setGuestVoucher(true);
                    customerPlatformVoucher.setStoreId(storeId);
                } else {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    response.setMessage("Voucher code " + platformVoucherCode + " not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Platform Voucher found : "+customerPlatformVoucher.getId());
                //check minimum amount
                //check vertical code
                //check double discount allowed
            }            
        } else {
            if (cod.getVoucherCode()!=null && !"".equals(cod.getVoucherCode())) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "COD VoucherCode provided : "+cod.getVoucherCode());
                customerPlatformVoucher = customerVoucherRepository.findCustomerPlatformVoucherByCode(cod.getCustomerId(), cod.getVoucherCode(), new Date());
                if (customerPlatformVoucher==null) {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    response.setMessage("Voucher code " + cod.getVoucherCode() + " not found");
                    return ResponseEntity.status(response.getStatus()).body(response);
                }
            }
        }
            
        //check store voucher code if provided
        CustomerVoucher customerStoreVoucher = null;
        if (cod.getStoreVoucherCode()!=null && !"".equals(cod.getStoreVoucherCode())) {
            customerStoreVoucher = customerVoucherRepository.findCustomerStoreVoucherByCode(cod.getCustomerId(), cod.getStoreVoucherCode(), new Date());
            if (customerStoreVoucher==null) {                
                //find guest voucher
                Voucher guestVoucher = customerVoucherRepository.findGuestStoreVoucherByCode(cod.getStoreVoucherCode(), new Date());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store Guest Voucher:"+guestVoucher);
                if (guestVoucher!=null) {
                    //check if already redeem
                    if (cod.getOrderShipmentDetails().getEmail()!=null) {
                        List<CustomerVoucher> usedVoucherList = null;
                        if (storeId!=null) {
                            usedVoucherList = customerVoucherRepository.findByGuestEmailAndVoucherIdAndStoreId(cod.getOrderShipmentDetails().getEmail(), guestVoucher.getId(), storeId);
                        } else {
                            usedVoucherList = customerVoucherRepository.findByGuestEmailAndVoucherId(cod.getOrderShipmentDetails().getEmail(), guestVoucher.getId());
                        }
                        if (usedVoucherList.size()>0) {                        
                            CustomerVoucher usedVoucher = usedVoucherList.get(0);
                            if (usedVoucher.getIsUsed() && !guestVoucher.getAllowMultipleRedeem()) {
                                //already used
                                response.setStatus(HttpStatus.NOT_FOUND.value());
                                response.setMessage("Sorry, you have redeemed this voucher");
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                            } else {
                                customerStoreVoucher = usedVoucher;
                            }
                        } else {
                            customerStoreVoucher = new CustomerVoucher();
                            customerStoreVoucher.setGuestEmail(cod.getOrderShipmentDetails().getEmail());
                            customerStoreVoucher.setIsUsed(false);
                            customerStoreVoucher.setVoucherId(guestVoucher.getId());
                            customerStoreVoucher.setCreated(new Date());
                            customerStoreVoucher.setGuestVoucher(true);
                            customerStoreVoucher.setStoreId(storeId);
                            customerStoreVoucher.setVoucher(guestVoucher);
                            customerVoucherRepository.save(customerStoreVoucher);
                        }
                    } else {
                        response.setStatus(HttpStatus.NOT_FOUND.value());
                        response.setMessage("Please insert email");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    }
                } else {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    response.setMessage("Sorry, Store Voucher code " + cod.getStoreVoucherCode() + " not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                
            }             
        }
        
        StoreWithDetails storeWithDetials = null;
        Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(optCart.get().getStoreId());
        if (optStore.isPresent()) {
            storeWithDetials = optStore.get();
        } else {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "store with storeId: " + optCart.get().getStoreId() + " not found");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("store not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        
        StoreDeliveryDetail storeDeliveryDetail = null;
        Optional<StoreDeliveryDetail> optStoreDeliveryDetail = storeDeliveryDetailRepository.findByStoreId(optCart.get().getStoreId());        
        if (optStoreDeliveryDetail.isPresent()) {
            storeDeliveryDetail = optStoreDeliveryDetail.get();
        } else {        
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("store delivery detail not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        }

        //set isCombinedDelivery=false for single order
        cod.getOrderPaymentDetails().setIsCombinedDelivery(false);
        
        response = OrderWorker.placeOrder(
                request.getRequestURI(), optCart.get(), cartItems, 
                cod, storeWithDetials, storeDeliveryDetail,
                customerStoreVoucher,
                saveCustomerInformation, 
                sendReceiptToReceiver,
                channel,
                onboardingOrderLink, orderInvoiceBaseUrl, logprefix, 
                cartRepository, cartItemRepository, customerVoucherRepository, 
                storeDetailsRepository, storeDeliveryDetailRepository, 
                productInventoryRepository, storeDiscountRepository, storeDiscountTierRepository, 
                orderRepository, orderPaymentDetailRepository, orderShipmentDetailRepository, 
                orderItemRepository, orderSubItemRepository, orderItemAddOnRepository, voucherRepository, storeRepository, 
                regionCountriesRepository, customerRepository, 
                orderCompletionStatusConfigRepository, 
                productService, orderPostService, fcmService, 
                emailService, deliveryService, customerService, whatsappService, assetServiceBaseUrl);  
        
        if (response.getStatus()==HttpStatus.CREATED.value()) {
            Order orderCreated = (Order)response.getData();  
            String customerId = orderCreated.getCustomerId();
            
            OrderGroup orderGroup = new OrderGroup();
            orderGroup.setCustomerId(customerId);
            orderGroup.setDeliveryCharges(orderCreated.getDeliveryCharges());
            OrderObject totalDataObject = orderCreated.getTotalDataObject();
            
            double orderTotal=0.00;
            boolean gotItemDiscount=false;
            for (int z=0;z<cartItems.size();z++) {
                CartItem cartItem = cartItems.get(z);
                if (cartItem.getDiscountId()!=null) {
                    gotItemDiscount=true;
                }
            }
            String cartServiceType = "";
            if (orderCreated.getServiceType()!=null) {
                cartServiceType = orderCreated.getServiceType().name();
            }
            OrderObject groupTotal = OrderCalculation.CalculateGroupOrderTotal(
                    orderCreated.getSubTotal(), orderCreated.getAppliedDiscount(), 
                    orderCreated.getDeliveryCharges(), orderCreated.getDeliveryDiscount(), 
                    customerPlatformVoucher, orderCreated.getStoreServiceCharges(), 
                    logprefix, gotItemDiscount,
                    storeWithDetials.getVerticalCode(), cartServiceType);            
            if (groupTotal.getGotError()) {
                // should return warning if got error
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error while calculating discount:"+groupTotal.getErrorMessage());
                response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
                response.setMessage(groupTotal.getErrorMessage());
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
            }
            
            if (groupTotal.getVoucherId()!=null) {
                double platformVoucherDiscountAmt = groupTotal.getVoucherDiscount();
                orderGroup.setPlatformVoucherDiscount(platformVoucherDiscountAmt);
                orderGroup.setPlatformVoucherId(customerPlatformVoucher.getVoucherId());
                orderTotal = totalDataObject.getTotal() - platformVoucherDiscountAmt;                
            } else {
                orderTotal = totalDataObject.getTotal();
            }
            
            //save customer voucher in account
            if (customerPlatformVoucher !=null && customerPlatformVoucher.getGuestVoucher()!=null && customerPlatformVoucher.getGuestVoucher()) {
                if (orderCreated.getPaymentType().equals(StorePaymentType.COD.name())) {
                    customerPlatformVoucher.setIsUsed(true);
                    voucherRepository.deductVoucherBalance(customerPlatformVoucher.getVoucherId());
                }
                customerVoucherRepository.save(customerPlatformVoucher);
            }
            
            orderGroup.setSubTotal(totalDataObject.getSubTotal());
            orderGroup.setTotal(orderTotal);
            orderGroup.setAppliedDiscount(orderCreated.getAppliedDiscount());
            orderGroup.setDeliveryDiscount(orderCreated.getDeliveryDiscount());
            if (orderCreated.getPaymentType().equals(StorePaymentType.COD.name())) {
                orderGroup.setPaymentStatus("PAID");
                orderGroup.setPaidAmount(orderTotal);
            } else {
                orderGroup.setPaymentStatus("PENDING");
                orderGroup.setPaidAmount(0.00);
            }
            orderGroup.setRegionCountryId(storeWithDetials.getRegionCountryId());
            if (cart.getServiceType()!=null)
                orderGroup.setServiceType(cart.getServiceType());
            else
                orderGroup.setServiceType(ServiceType.DELIVERIN);
            
            if (channel!=null)
                orderGroup.setChannel(channel);
            else
                orderGroup.setChannel(Channel.DELIVERIN);
                         
            orderGroupRepository.save(orderGroup);
            
            orderRepository.UpdateOrderGroupId(orderCreated.getId(), orderGroup.getId());
                                  
            if (orderGroup.getServiceType()==ServiceType.DINEIN && qrToken!=null  && tableNo!=null && !tableNo.equals("")) {            
                QrcodeOrderGroup qrOrder = qrcodeOrderGroupRepository.findByQrToken(qrToken);
                if (qrOrder==null) {
                    //create new qr group order
                    qrOrder = new QrcodeOrderGroup();
                    String invoiceId = TxIdUtil.generateGroupInvoiceNo(storeWithDetials.getId(), "DN", storeRepository);                
                    qrOrder.setQrToken(qrToken);
                    qrOrder.setTableNo(tableNo);                
                    qrOrder.setInvoiceNo(invoiceId);
                    qrOrder.setStoreId(storeWithDetials.getId()); 
                    qrOrder.setPaymentStatus(PaymentStatus.PENDING);
                    qrOrder.setSubTotal(orderCreated.getSubTotal());
                    qrOrder.setAppliedDiscount(orderCreated.getAppliedDiscount());
                    qrOrder.setDeliveryCharges(orderCreated.getDeliveryCharges());
                    qrOrder.setDeliveryDiscount(orderCreated.getDeliveryDiscount());
                    qrOrder.setServiceCharges(orderCreated.getStoreServiceCharges());
                    qrOrder.setTotalAmount(orderCreated.getTotal());
                    qrcodeOrderGroupRepository.save(qrOrder);
                } else {
                    double subTotal = qrOrder.getSubTotal() + orderCreated.getSubTotal();
                    double appliedDiscount = qrOrder.getAppliedDiscount() + orderCreated.getAppliedDiscount();
                    double deliverCharge = qrOrder.getDeliveryCharges() + orderCreated.getDeliveryCharges();
                    double deliveryDiscount = qrOrder.getDeliveryDiscount() + orderCreated.getDeliveryDiscount();
                    double serviceCharge = qrOrder.getServiceCharges() + orderCreated.getStoreServiceCharges();
                    double totalOrderAmount = qrOrder.getTotalOrderAmount() + orderCreated.getTotal();
                    qrOrder.setSubTotal(subTotal);
                    qrOrder.setAppliedDiscount(appliedDiscount);
                    qrOrder.setDeliveryCharges(deliverCharge);
                    qrOrder.setDeliveryDiscount(deliveryDiscount);
                    qrOrder.setServiceCharges(serviceCharge);
                    qrOrder.setTotalAmount(totalOrderAmount);
                    qrcodeOrderGroupRepository.save(qrOrder);
                }
                
                if (orderGroup.getServiceType()==ServiceType.DINEIN && qrToken!=null) {
                  orderGroupRepository.UpdateQrcodeOrderGroupId(qrOrder.getId(), orderGroup.getId());
                }
                
            }
           
            orderCreated.setId(orderCreated.getId());
            
            orderCreated.setOrderGroupId("G"+orderGroup.getId());
            response.setData(orderCreated);
        }
        return ResponseEntity.status(response.getStatus()).body(response);
                
    }    

    
     /**
     *
     *
     * @param request
     * @param saveCustomerInformation
     * @param platformVoucherCode
     * @param sendReceiptToReceiver
     * @param channel
     * @param qrToken
     * @param tableNo
     * @param isStaffOrder
     * @param serviceType
     * @param codList
     * @return
     * @throws Exception
     */
    @PostMapping(path = {"/placeGroupOrder"}, name = "orders-push-cod")
    @PreAuthorize("hasAnyAuthority('orders-push-cod', 'all')")
    public ResponseEntity<HttpResponse> placeGroupOrder(HttpServletRequest request,
            @RequestParam(required = false) Boolean saveCustomerInformation,
            @RequestParam(required = false) String platformVoucherCode,
            @RequestParam(required = false) Boolean sendReceiptToReceiver,
            @RequestParam(required = false) Channel channel,
            @RequestParam(required = false) String qrToken,
            @RequestParam(required = false) String tableNo,
            @RequestParam(required = false) Boolean isStaffOrder,
            @RequestBody COD[] codList) throws Exception {
        String logprefix = request.getRequestURI() + " ";
       
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orders-push-group request on url: " + request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orders-push-group request body: " + codList.toString());
        
        HttpResponse response = new HttpResponse(request.getRequestURI());        
        
        if (isStaffOrder!=null && isStaffOrder) {
            //create new cart and add item into cart
            for (int z=0;z<codList.length;z++) {            
                COD cod = codList[z];                
                //create new cart
                Cart cart = new Cart();
                cart.setCustomerId(cod.getCustomerId());
                cart.setStoreId(cod.getStoreId());
                cart.setServiceType(cod.getServiceType());
                cart.setStage(CartStage.ORDER_PLACED);
                cart.setIsOpen(false);
                cart = cartRepository.save(cart);
                String cartId = cart.getId();
                cod.setCartId(cartId);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Set cartId:"+cod.getCartId());
                
                //add item into cart
                List<CartItem> cartItemList = cod.getCartItems();                
                for (int x=0;x<cartItemList.size();x++) {
                    CartItem inputCartItem = cartItemList.get(x);
                    
                    addItemToCart(cod.getStoreId(), inputCartItem.getProductId(), 
                        inputCartItem.getItemCode(), inputCartItem.getQuantity(), 
                        cartId,
                        ServiceType.DINEIN, inputCartItem.getSpecialInstruction());            
                }
                
            }
           
        }
        
        //get customer id from one of COD
        String customerId = null;
        String customerEmail = null;
        if (codList.length>0) {
            customerId = codList[0].getCustomerId();
            if (codList[0].getOrderShipmentDetails()!=null) {
                customerEmail = codList[0].getOrderShipmentDetails().getEmail();
            }
        }
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "CustomerId:"+customerId+" customerEmail:"+customerEmail);
        
        //check platform voucher code if provided
        CustomerVoucher customerPlatformVoucher = null;
        if (platformVoucherCode!=null && !"".equals(platformVoucherCode)) {
            customerPlatformVoucher = customerVoucherRepository.findCustomerPlatformVoucherByCode(customerId, platformVoucherCode, new Date());
            if (customerPlatformVoucher==null) {
                //find guest voucher
                Voucher guestVoucher = customerVoucherRepository.findGuestPlatformVoucherByCode(platformVoucherCode, new Date());
                if (guestVoucher!=null) {
                    //check if already redeem
                    List<CustomerVoucher> usedVoucherList = customerVoucherRepository.findByGuestEmailAndVoucherId(customerEmail, guestVoucher.getId());
                    if (usedVoucherList.size()>0) {  
                        CustomerVoucher usedVoucher = usedVoucherList.get(0);
                        if (usedVoucher.getIsUsed()) {
                            //already used
                            response.setStatus(HttpStatus.NOT_FOUND.value());
                            response.setMessage("Voucher code " + platformVoucherCode + " already used");
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } 
                        customerPlatformVoucher = usedVoucher;
                    } else {
                        customerPlatformVoucher = new CustomerVoucher();
                        customerPlatformVoucher.setCreated(new Date());
                    } 
                    customerPlatformVoucher.setGuestEmail(customerEmail);
                    customerPlatformVoucher.setIsUsed(false);
                    customerPlatformVoucher.setVoucherId(guestVoucher.getId());
                    customerPlatformVoucher.setVoucher(guestVoucher);
                    customerPlatformVoucher.setGuestVoucher(true);
                } else {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    response.setMessage("Voucher code " + platformVoucherCode + " not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            } else {
                //check minimum amount
                //check vertical code
                //check double discount allowed
            }
        }
        
        String qrStoreId=null;
        
        //validate every cart in the group
        for (int z=0;z<codList.length;z++) {            
            COD cod = codList[z];
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Cod["+z+"] : "+cod);
            String cartId = codList[z].getCartId();
            
            Optional<Cart> optCart = cartRepository.findById(cartId);
            if (!optCart.isPresent()) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart with id " + cartId + " not found");
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("Cart with id " + cartId + " not found");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
            
            //update cart status
            Cart cart = optCart.get();
            cart.setStage(CartStage.ORDER_PLACED);
            cartRepository.save(cart);

            // get cart items based on selected item
            List<CartItem> cartItems = cod.getCartItems();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got cartItems of cartId: " + cartId + ", items: " + cartItems.toString());

            //if cart empty
            if (cartItems.isEmpty()) {
                response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                response.setMessage("Cart is empty");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
 
            //check store voucher code if provided
            CustomerVoucher customerStoreVoucher = null;
            if (cod.getStoreVoucherCode()!=null && !"".equals(cod.getStoreVoucherCode())) {
                customerStoreVoucher = customerVoucherRepository.findCustomerStoreVoucherByCode(cod.getCustomerId(), cod.getStoreVoucherCode(), new Date());
                if (customerStoreVoucher==null) {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    response.setMessage("Voucher code " + cod.getStoreVoucherCode() + " not found");
                    return ResponseEntity.status(response.getStatus()).body(response);
                } else {
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
                        response.setMessage("Voucher code " + cod.getStoreVoucherCode() + " cannot be used for this store");
                        return ResponseEntity.status(response.getStatus()).body(response);
                    }     
                }
            }

            StoreWithDetails storeWithDetials = null;
            Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(optCart.get().getStoreId());
            if (optStore.isPresent()) {
                storeWithDetials = optStore.get();
                qrStoreId = storeWithDetials.getId();
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "store with storeId: " + optCart.get().getStoreId() + " not found");
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("store not found");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
        
            StoreDeliveryDetail storeDeliveryDetail = null;
            Optional<StoreDeliveryDetail> optStoreDeliveryDetail = storeDeliveryDetailRepository.findByStoreId(optCart.get().getStoreId());        
            if (optStoreDeliveryDetail.isPresent()) {
                storeDeliveryDetail = optStoreDeliveryDetail.get();
            } else {        
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("store delivery detail not found");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
        } 

        //create new group order object
        OrderGroup orderGroup = new OrderGroup();
        List<Order> orderCreatedList = new ArrayList();
        double sumCartSubTotal=0.00;
        double sumDeliveryCharges=0.00;
        double sumTotal=0.00;
        double orderTotal=0.00;
        double sumAppliedDiscount=0.00;
        double sumDeliveryDiscount=0.00;
        double sumStoreServiceCharges=0.00;
        double sumStoreVoucherDiscount=0.00;
        String paymentType=StorePaymentType.ONLINEPAYMENT.name();
        boolean gotCartItemDiscount=false;
        Map<String, Double> combinedDeliveryFeeMap = new HashMap<String, Double>();
        
        String regionCountryId="MYS";
        String cartVerticalCode="";
        String cartServiceType="";
        boolean consolidateOrder=false;
        String staffId=null;
        for (int i=0;i<codList.length;i++) {
            COD cod = codList[i];
            String cartId = cod.getCartId();
            Optional<Cart> optCart = cartRepository.findById(cartId);            
            Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(optCart.get().getStoreId());
            Optional<StoreDeliveryDetail> optStoreDeliveryDetail = storeDeliveryDetailRepository.findByStoreId(optCart.get().getStoreId());        
            CustomerVoucher customerStoreVoucher = customerVoucherRepository.findCustomerStoreVoucherByCode(cod.getCustomerId(), cod.getStoreVoucherCode(), new Date());
            
            List<CartItem> selectedCartItem = new ArrayList<>();
            for (int x=0;x<cod.getCartItems().size();x++) {
                String itemId = cod.getCartItems().get(x).getId();
                Optional<CartItem> cartItemOpt = cartItemRepository.findById(itemId);
                if (cartItemOpt.isPresent()) {
                    selectedCartItem.add(cartItemOpt.get());
                    if (cartItemOpt.get().getDiscountId()!=null) {
                        gotCartItemDiscount=true;
                    }
                } else {
                    //cart item not found in db
                    response.setStatus(HttpStatus.EXPECTATION_FAILED.value());
                    response.setMessage("Cart item not found");
                    return ResponseEntity.status(response.getStatus()).body(response);
                }
            }
            
            if (cod.getOrderShipmentDetails()==null) {
                OrderShipmentDetail orderShipmentDetails = new OrderShipmentDetail();
                orderShipmentDetails.setStorePickup(true);
                cod.setOrderShipmentDetails(orderShipmentDetails);
            }
            if (cod.getOrderPaymentDetails()==null) {
                OrderPaymentDetail orderPaymentDetails = new OrderPaymentDetail();
                cod.setOrderPaymentDetails(orderPaymentDetails);
            }
            
            HttpResponse orderResponse = OrderWorker.placeOrder(
                    request.getRequestURI(), optCart.get(), selectedCartItem, cod, optStore.get(), optStoreDeliveryDetail.get(),
                    customerStoreVoucher,
                    saveCustomerInformation, 
                    sendReceiptToReceiver,
                    channel,
                    onboardingOrderLink, orderInvoiceBaseUrl, logprefix, 
                    cartRepository, cartItemRepository, customerVoucherRepository, 
                    storeDetailsRepository, storeDeliveryDetailRepository, 
                    productInventoryRepository, storeDiscountRepository, storeDiscountTierRepository, 
                    orderRepository, orderPaymentDetailRepository, orderShipmentDetailRepository, 
                    orderItemRepository, orderSubItemRepository, orderItemAddOnRepository, voucherRepository, storeRepository, 
                    regionCountriesRepository, customerRepository, 
                    orderCompletionStatusConfigRepository, 
                    productService, orderPostService, fcmService, 
                    emailService, deliveryService, customerService, whatsappService, assetServiceBaseUrl);  
            Order orderCreated = (Order)orderResponse.getData();
            if (orderCreated==null) {                
                return ResponseEntity.status(orderResponse.getStatus()).body(orderResponse);
            }
            sumCartSubTotal = sumCartSubTotal + orderCreated.getSubTotal();
            if (orderCreated.getOrderPaymentDetail().getIsCombinedDelivery()) {
                combinedDeliveryFeeMap.put(orderCreated.getOrderPaymentDetail().getDeliveryQuotationReferenceId(), orderCreated.getDeliveryCharges());
            } else {
                sumDeliveryCharges = sumDeliveryCharges + orderCreated.getDeliveryCharges();
            }
            sumStoreServiceCharges = sumStoreServiceCharges + orderCreated.getStoreServiceCharges();
            //move to below orderTotal = orderTotal + orderCreated.getTotal();
            if (orderCreated.getAppliedDiscount()!=null) {
                sumAppliedDiscount = sumAppliedDiscount + orderCreated.getAppliedDiscount();
            }
            if (orderCreated.getDeliveryDiscount()!=null) {
                sumDeliveryDiscount = sumDeliveryDiscount + orderCreated.getDeliveryDiscount();
            }
            if (orderCreated.getStoreVoucherDiscount()!=null) {
                sumStoreVoucherDiscount = sumStoreVoucherDiscount + orderCreated.getStoreVoucherDiscount();
            }
            orderCreatedList.add(orderCreated);
            paymentType = orderCreated.getPaymentType();
            regionCountryId = optStore.get().getRegionCountryId();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order Created SubTotal:"+orderCreated.getSubTotal()+" deliverFee:"+orderCreated.getDeliveryCharges()+" svcCharge:"+orderCreated.getStoreServiceCharges()+" Total:"+orderCreated.getTotal());
        
            cartVerticalCode = optStore.get().getVerticalCode();
            if (orderCreated.getServiceType()!=null) {
                cartServiceType = orderCreated.getServiceType().name();
            }
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Consolidate Order:"+optStore.get().getDineInConsolidatedOrder());
            if (optStore.get().getDineInConsolidatedOrder()!=null && optStore.get().getDineInConsolidatedOrder()==true) {
                consolidateOrder=true;
            }
            
            staffId = cod.getStaffId();
        }       
        for (Map.Entry<String, Double> combinedDelivery :
            combinedDeliveryFeeMap.entrySet()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DeliveryQuotationId:"+combinedDelivery.getKey()+" Amount:"+combinedDelivery.getValue());
            sumDeliveryCharges = sumDeliveryCharges + combinedDelivery.getValue();
        }
                
        OrderObject groupTotal = OrderCalculation.CalculateGroupOrderTotal(
                sumCartSubTotal, sumAppliedDiscount, 
                sumDeliveryCharges, sumDeliveryDiscount, 
                customerPlatformVoucher, sumStoreServiceCharges, 
                logprefix, gotCartItemDiscount,
                cartVerticalCode, cartServiceType);
        if (groupTotal.getGotError()) {
            // should return warning if got error
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error while calculating discount:"+groupTotal.getErrorMessage());
            response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
            response.setMessage(groupTotal.getErrorMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        
        //check if group order already exist
        
        //calculate grand total
        orderTotal = sumCartSubTotal - sumAppliedDiscount + sumStoreServiceCharges + sumDeliveryCharges - sumDeliveryDiscount - sumStoreVoucherDiscount;
        
        if (groupTotal.getVoucherId()!=null) {
            double platformVoucherDiscountAmt = groupTotal.getVoucherDiscount();
            orderGroup.setPlatformVoucherDiscount(platformVoucherDiscountAmt);
            orderGroup.setPlatformVoucherId(groupTotal.getVoucherId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order Group platformVoucherDiscountAmt:"+platformVoucherDiscountAmt);
            sumTotal = orderTotal - platformVoucherDiscountAmt;           
        } else {
            sumTotal = orderTotal;
        }
        
        //get shipment details from one of order created
        OrderShipmentDetail orderShipmentDetail = orderCreatedList.get(0).getOrderShipmentDetail();
        customerId = orderCreatedList.get(0).getCustomerId();
        ServiceType serviceType = orderCreatedList.get(0).getServiceType();
        
        orderGroup.setCustomerId(customerId);
        orderGroup.setDeliveryCharges(sumDeliveryCharges);  
        orderGroup.setServiceCharges(sumStoreServiceCharges);
        orderGroup.setSubTotal(sumCartSubTotal);
        orderGroup.setTotal(sumTotal);
        orderGroup.setAppliedDiscount(sumAppliedDiscount);
        orderGroup.setDeliveryDiscount(sumDeliveryDiscount);
        orderGroup.setShipmentEmail(orderShipmentDetail.getEmail());
        orderGroup.setShipmentName(orderShipmentDetail.getReceiverName());
        orderGroup.setShipmentPhoneNumber(orderShipmentDetail.getPhoneNumber());
        orderGroup.setRegionCountryId(regionCountryId);
        if (paymentType.equals(StorePaymentType.COD.name())) {
            orderGroup.setPaymentStatus("PAID");
            orderGroup.setPaidAmount(sumTotal);
        } else {
            orderGroup.setPaymentStatus("PENDING");
            orderGroup.setPaidAmount(0.00);
        }
        orderGroup.setServiceType(serviceType);
        if (channel!=null)
            orderGroup.setChannel(channel);
        else
            orderGroup.setChannel(Channel.DELIVERIN);
        
        if (isStaffOrder!=null && isStaffOrder) {
            orderGroup.setStaffId(staffId);
        }
        
        Long qrGroupOrderId = null;
        if (orderGroup.getServiceType()==ServiceType.DINEIN && consolidateOrder==true && tableNo!=null && !tableNo.equals("")) {            
            QrcodeOrderGroup qrOrder = qrcodeOrderGroupRepository.findByStoreIdAndTableNoAndPaymentStatus(qrStoreId, tableNo, PaymentStatus.PENDING);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Consolidate Order. Previous qrOrderGroup:"+qrOrder);
            if (qrOrder==null) {
                //create new qr group order
                qrOrder = new QrcodeOrderGroup();
                String invoiceId = TxIdUtil.generateGroupInvoiceNo(qrStoreId, "DN", storeRepository);                
                qrOrder.setQrToken(qrToken);
                qrOrder.setTableNo(tableNo);                
                qrOrder.setInvoiceNo(invoiceId);
                qrOrder.setStoreId(qrStoreId); 
                qrOrder.setPaymentStatus(PaymentStatus.PENDING);
                qrOrder.setSubTotal(orderGroup.getSubTotal());
                qrOrder.setAppliedDiscount(orderGroup.getAppliedDiscount());
                qrOrder.setDeliveryCharges(orderGroup.getDeliveryCharges());
                qrOrder.setDeliveryDiscount(orderGroup.getDeliveryDiscount());
                qrOrder.setServiceCharges(orderGroup.getServiceCharges());
                qrOrder.setTotalAmount(orderGroup.getTotal());
                qrcodeOrderGroupRepository.save(qrOrder);
            } else {
                double subTotal = qrOrder.getSubTotal() + orderGroup.getSubTotal();
                double appliedDiscount = qrOrder.getAppliedDiscount() + orderGroup.getAppliedDiscount();
                double deliverCharge = qrOrder.getDeliveryCharges() + orderGroup.getDeliveryCharges();
                double deliveryDiscount = qrOrder.getDeliveryDiscount() + orderGroup.getDeliveryDiscount();
                double serviceCharge = qrOrder.getServiceCharges() + orderGroup.getServiceCharges();
                double totalOrderAmount = qrOrder.getTotalOrderAmount() + orderGroup.getTotal();
                qrOrder.setSubTotal(subTotal);
                qrOrder.setAppliedDiscount(appliedDiscount);
                qrOrder.setDeliveryCharges(deliverCharge);
                qrOrder.setDeliveryDiscount(deliveryDiscount);
                qrOrder.setServiceCharges(serviceCharge);
                qrOrder.setTotalAmount(totalOrderAmount);
            }            
            qrGroupOrderId = qrOrder.getId();
        }
        
        orderGroupRepository.save(orderGroup);
               
        //update orderGroupId for each order
        for (int x=0;x<orderCreatedList.size();x++) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Update OrderGroupId="+orderGroup.getId()+" for OrderId:"+orderCreatedList.get(x).getId());
            orderRepository.UpdateOrderGroupId(orderCreatedList.get(x).getId(), orderGroup.getId());            
        }  
        if (orderGroup.getServiceType()==ServiceType.DINEIN && qrGroupOrderId!=null && tableNo!=null && !tableNo.equals("")) {            
            orderGroupRepository.UpdateQrcodeOrderGroupId(qrGroupOrderId, orderGroup.getId());
        }
        
        //save customer voucher in account
        if (customerPlatformVoucher!=null && customerPlatformVoucher.getGuestVoucher()!=null && customerPlatformVoucher.getGuestVoucher()) {
            if (orderCreatedList.get(0).getPaymentType().equals(StorePaymentType.COD.name())) {
                customerPlatformVoucher.setIsUsed(true);
                voucherRepository.deductVoucherBalance(customerPlatformVoucher.getVoucherId());
            }            
            customerVoucherRepository.save(customerPlatformVoucher);
        }
            
        //append prefix to differnetiate between single & multiple
        orderGroup.setId("G"+orderGroup.getId());
        
        response.setStatus(HttpStatus.CREATED.value());
        response.setData(orderGroup);
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "placeGroupOrder completed");
         
        return ResponseEntity.status(response.getStatus()).body(response);
    }            
    
    @DeleteMapping(path = {"/{id}"}, name = "orders-delete-by-id")
    @PreAuthorize("hasAnyAuthority('orders-delete-by-id', 'all') and @customOwnerVerifier.VerifyOrder(#id)")
    public ResponseEntity<HttpResponse> deleteOrdersById(HttpServletRequest request,
            @PathVariable(required = true) String id) {
        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orders-delete-by-id, orderId: " + id);

        Optional<Order> optProduct = orderRepository.findById(id);

        if (!optProduct.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order not found with orderId: " + id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order found", "");
        orderRepository.deleteById(id);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order deleted, with orderId: " + id);
        response.setSuccessStatus(HttpStatus.OK);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     *
     * @param request
     * @param id
     * @param bodyOrder
     * @return
     */
    @PutMapping(path = {"/{id}"}, name = "orders-put-by-id")
    @PreAuthorize("hasAnyAuthority('orders-put-by-id', 'all') and @customOwnerVerifier.VerifyOrder(#id)")
    public ResponseEntity<HttpResponse> putOrdersById(HttpServletRequest request,
            @PathVariable String id,
            @RequestBody Order bodyOrder) {
        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "", "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrder.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(id);

        if (!optOrder.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: " + id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order found with orderId: " + id);
        Order order = optOrder.get();
        List<String> errors = new ArrayList<>();

        order.update(bodyOrder);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order updated for orderId: " + id);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderRepository.save(order));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    void updateQuantityInInventory(ProductInventory productInventory, int quantity) {
        String logprefix = "updateQuantityInInventory";
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "productInventoryId: " + productInventory.getItemCode() + "productQuantity: " + productInventory.getQuantity());
        int newQuantity = productInventory.getQuantity() - quantity;
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "productInventoryId: " + productInventory.getItemCode() + "newQuantity: " + newQuantity);
        productInventory.setQuantity(quantity);
    }

    /**
     * Accept two dates and example matcher
     *
     * @param from
     * @param to
     * @param example
     * @return
     */
    public Specification<Order> getSpecWithDatesBetween(
            Date from, Date to, OrderStatus completionStatus, Example<Order> example) {

        return (Specification<Order>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("created"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("created"), to));
            }
           
            if (completionStatus==OrderStatus.PAYMENT_CONFIRMED) {
                Predicate predicateForOnlinePayment = builder.equal(root.get("completionStatus"), completionStatus);
                Predicate predicateForCompletionStatus = builder.equal(root.get("completionStatus"), OrderStatus.RECEIVED_AT_STORE);
                Predicate predicateForPaymentType = builder.equal(root.get("paymentType"), "COD");
                Predicate predicateForCOD = builder.and(predicateForCompletionStatus, predicateForPaymentType);
                Predicate finalPredicate = builder.or(predicateForOnlinePayment, predicateForCOD);
                predicates.add(finalPredicate);
            } else if (completionStatus!=null) {
                predicates.add(builder.equal(root.get("completionStatus"), completionStatus));
            }
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
    
    /**
     * Accept two dates and example matcher
     *
     * @param from
     * @param to
     * @param example
     * @return
     */
    public Specification<OrderWithDetails> getOrderDetailsSpecWithDatesBetween(
            Date from, Date to, OrderStatus completionStatus, Example<OrderWithDetails> example) {

        return (Specification<OrderWithDetails>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("created"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("created"), to));
            }
           
            if (completionStatus==OrderStatus.PAYMENT_CONFIRMED) {
                Predicate predicateForOnlinePayment = builder.equal(root.get("completionStatus"), completionStatus);
                Predicate predicateForCompletionStatus = builder.equal(root.get("completionStatus"), OrderStatus.RECEIVED_AT_STORE);
                Predicate predicateForPaymentType = builder.equal(root.get("paymentType"), "COD");
                Predicate predicateForCOD = builder.and(predicateForCompletionStatus, predicateForPaymentType);
                Predicate finalPredicate = builder.or(predicateForOnlinePayment, predicateForCOD);
                predicates.add(finalPredicate);
            } else if (completionStatus!=null) {
                predicates.add(builder.equal(root.get("completionStatus"), completionStatus));
            }
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
    
    /**
     * Accept two dates and example matcher
     *
     * @param from
     * @param to
     * @param example
     * @return
     */
    public Specification<Order> getSpecWithDatesBetweenMultipleStatus(
            Date from, Date to, OrderStatus[] completionStatusList, Example<Order> example) {

        return (Specification<Order>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("created"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("created"), to));
            }
            
            if (completionStatusList!=null) {
                int statusCount = completionStatusList.length;
                List<Predicate> statusPredicatesList = new ArrayList<>();
                for (int i=0;i<completionStatusList.length;i++) {
                    Predicate predicateForCompletionStatus = builder.equal(root.get("completionStatus"), completionStatusList[i]);
                                        
                    if (completionStatusList[i]==OrderStatus.RECEIVED_AT_STORE) {
                        Predicate predicateForStatus = builder.equal(root.get("completionStatus"), OrderStatus.RECEIVED_AT_STORE);
                        Predicate predicateForPaymentType = builder.equal(root.get("paymentType"), "COD");
                        Predicate predicateForCOD = builder.and(predicateForStatus, predicateForPaymentType);
                        statusPredicatesList.add(predicateForCOD);            
                    } else if (completionStatusList[i]!=null) {
                        statusPredicatesList.add(predicateForCompletionStatus);
                    }
                }

                Predicate finalPredicate = builder.or(statusPredicatesList.toArray(new Predicate[statusCount]));
                predicates.add(finalPredicate);
            }
            
            /*
            if (completionStatusList==OrderStatus.PAYMENT_CONFIRMED) {
                Predicate predicateForOnlinePayment = builder.equal(root.get("completionStatus"), completionStatus);
                Predicate predicateForCompletionStatus = builder.equal(root.get("completionStatus"), OrderStatus.RECEIVED_AT_STORE);
                Predicate predicateForPaymentType = builder.equal(root.get("paymentType"), "COD");
                Predicate predicateForCOD = builder.and(predicateForCompletionStatus, predicateForPaymentType);
                Predicate finalPredicate = builder.or(predicateForOnlinePayment, predicateForCOD);
                predicates.add(finalPredicate);
            } else if (completionStatusList!=null) {
                predicates.add(builder.equal(root.get("completionStatus"), completionStatus));
            }*/
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
    
    
    /**
     * Accept two dates and example matcher
     *
     * @param from
     * @param to
     * @param example
     * @param completionStatusList
     * @param idList
     * @param orderGroupIds
     * @return
     */
    public Specification<OrderWithDetails> getOrderWithDetailsSpecWithDatesBetweenMultipleStatus(
            Date from, Date to, OrderStatus[] completionStatusList, String[] idList, String[] orderGroupIds, Example<OrderWithDetails> example) {

        return (Specification<OrderWithDetails>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("created"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("created"), to));
            }
            
            if (completionStatusList!=null) {
                int statusCount = completionStatusList.length;
                List<Predicate> statusPredicatesList = new ArrayList<>();
                for (int i=0;i<completionStatusList.length;i++) {
                    Predicate predicateForCompletionStatus = builder.equal(root.get("completionStatus"), completionStatusList[i]);
                                        
                    if (completionStatusList[i]==OrderStatus.RECEIVED_AT_STORE) {
                        Predicate predicateForStatus = builder.equal(root.get("completionStatus"), OrderStatus.RECEIVED_AT_STORE);
                        Predicate predicateForPaymentType = builder.equal(root.get("paymentType"), "COD");
                        Predicate predicateForCOD = builder.and(predicateForStatus, predicateForPaymentType);
                        statusPredicatesList.add(predicateForCOD);            
                    } else if (completionStatusList[i]!=null) {
                        statusPredicatesList.add(predicateForCompletionStatus);
                    }
                }

                Predicate finalPredicate = builder.or(statusPredicatesList.toArray(new Predicate[statusCount]));
                predicates.add(finalPredicate);
            }
            
            if (idList!=null) {
                int idCount = idList.length;
                List<Predicate> orderIdPredicatesList = new ArrayList<>();
                for (int i=0;i<idList.length;i++) {
                    Predicate predicateForId = builder.equal(root.get("id"), idList[i]);
                    orderIdPredicatesList.add(predicateForId);                    
                }

                Predicate finalPredicate = builder.or(orderIdPredicatesList.toArray(new Predicate[idCount]));
                predicates.add(finalPredicate);
            }
            
            if (orderGroupIds!=null) {
                int idCount = orderGroupIds.length;
                List<Predicate> orderGroupIdPredicatesList = new ArrayList<>();
                for (int i=0;i<orderGroupIds.length;i++) {
                    if (orderGroupIds[i].startsWith("G")) {
                        orderGroupIds[i] = orderGroupIds[i].substring(1);
                    }
                    Predicate predicateForGroupId = builder.equal(root.get("orderGroupId"), orderGroupIds[i]);
                    orderGroupIdPredicatesList.add(predicateForGroupId);                    
                }

                Predicate finalPredicate = builder.or(orderGroupIdPredicatesList.toArray(new Predicate[idCount]));
                predicates.add(finalPredicate);
            }
                       
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "getOrderWithDetailsSpecWithDatesBetweenMultipleStatus", "Predicates:"+predicates.toString());
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
    
    
    @GetMapping(path = {"/countsummary/{storeId}"}, name = "orders-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get', 'all') and @customOwnerVerifier.VerifyStore(#storeId)")
    public ResponseEntity<HttpResponse> getCountSummary(HttpServletRequest request,
            @PathVariable(required = true) String storeId,
            @RequestParam(required = false) ServiceType serviceType
           ) {
        String logprefix = request.getRequestURI() + " ";
    
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orders-get request " + request.getRequestURL());
        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        Optional<Store> optStore = storeRepository.findById(storeId);

        if (!optStore.isPresent()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("store not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        class DataSummary {
            String completionStatus;
            Long count;
            
            public String getCompletionStatus() {
                return completionStatus;
            }
            
             public Long getCount() {
                return count;
            }
        }
        
        Store store = optStore.get();
        List<Object> dataSummaryList = new ArrayList<Object>();
        List<Object[]> countSummaryList = null;
        if (serviceType!=null && !serviceType.equals("")) {
            if (store.getPaymentType().equals("COD") || serviceType==ServiceType.DINEIN) {
                countSummaryList = orderRepository.getCountSummaryCODByServiceType(storeId, serviceType);
            } else {
                countSummaryList = orderRepository.getCountSummaryOnlinePaymentByServiceType(storeId, serviceType);
            }
        } else {
            if (store.getPaymentType().equals("COD")) {
                countSummaryList = orderRepository.getCountSummaryCOD(storeId);
            } else {
                countSummaryList = orderRepository.getCountSummaryOnlinePayment(storeId);
            }
        }
        for (int i=0;i<countSummaryList.size();i++) {
            Object[] summary = countSummaryList.get(i);
            DataSummary dataSummary = new DataSummary();
            dataSummary.completionStatus = String.valueOf(summary[0]);
            dataSummary.count = (Long)summary[1];
            dataSummaryList.add(dataSummary);
        }
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(dataSummaryList);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(path = {"/details/{id}"}, name = "orders-get-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get-by-id', 'all')")
    public ResponseEntity<HttpResponse> getOrdersDetailsById(HttpServletRequest request,
            @PathVariable(required = true) String id) {

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Optional<Order> optOrder = orderRepository.findById(id);
        if (!optOrder.isPresent()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("order with id " + id + " not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        String logprefix = request.getRequestURI() + " ";
        
        Order order = optOrder.get();
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setOrder(order);
        orderDetails.setCurrentCompletionStatus(order.getCompletionStatus().name());
        
        Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(order.getStoreId());
        StoreWithDetails storeWithDetails = optStore.get();
        String verticalId = storeWithDetails.getVerticalCode();
        Boolean storePickup = order.getOrderShipmentDetail().getStorePickup();
        
        String storeDeliveryType = storeWithDetails.getStoreDeliveryDetail().getType();
        if (order.getServiceType()!=null && order.getServiceType()==ServiceType.DINEIN) {
            storeDeliveryType = order.getDineInOption().name();
        }
            
        //get current status
        String currentStatus = order.getCompletionStatus().name();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Find config for current status VerticalId:"+verticalId+" Status:"+currentStatus+" storePickup:"+storePickup+" storeDeliveryType:"+storeDeliveryType+" paymentType:"+order.getPaymentType()+" dineInOption:"+order.getDineInOption());
        List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = null;
        orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, currentStatus, storePickup, storeDeliveryType, order.getPaymentType());
        
        OrderCompletionStatusConfig orderCompletionStatusConfig = null;
        if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
            Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for current status: " + currentStatus);            
        } else {        
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size());
            orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);
                
            //get next action
            OrderCompletionStatusConfig nextCompletionStatusConfig = null;
            int nextSequence = orderCompletionStatusConfig.getStatusSequence()+1;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Find config for next status VerticalId:"+verticalId+" NextSequence:"+nextSequence+" storePickup:"+storePickup+" storeDeliveryType:"+storeDeliveryType+" paymentType:"+order.getPaymentType()+" dineInOption:"+order.getDineInOption());
            List<OrderCompletionStatusConfig> nextActionCompletionStatusConfigs = null;
            nextActionCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusSequenceAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, nextSequence, storePickup, storeDeliveryType, order.getPaymentType());
           
            if (nextActionCompletionStatusConfigs == null || nextActionCompletionStatusConfigs.isEmpty()) {
                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for next sequence:"+nextSequence);
            } else {
                nextCompletionStatusConfig = nextActionCompletionStatusConfigs.get(0);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Next action status: " + nextCompletionStatusConfig.getStatus()+" sequence:"+nextCompletionStatusConfig.getStatusSequence());
                orderDetails.setNextCompletionStatus(nextCompletionStatusConfig.status);
                orderDetails.setNextActionText(nextCompletionStatusConfig.nextActionText);
            }
        }
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderDetails);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
        
    /**
     *
     * @param request
     * @param orderId
     * @param bodyOrderItemList
     * @return
     */
        
    @PutMapping(path = {"/reviseitem/{orderId}"}, name = "orders-put-by-id")
    //@PreAuthorize("hasAnyAuthority('orders-put-by-id', 'all') and @customOwnerVerifier.VerifyOrder(#id)")
    public ResponseEntity<HttpResponse> reviseOrderItems(HttpServletRequest request,
            @PathVariable String orderId,
            @Valid @RequestBody OrderItem[] bodyOrderItemList) {
        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "reviseOrderItems()", "");
        
        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: " + orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            response.setMessage("Order not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order found with orderId: " + orderId);
        Order order = optOrder.get();
        
        PaymentOrder paymentOrder = null;
        if (order.getPaymentType().equals(StorePaymentType.ONLINEPAYMENT.name())) {
            Optional<PaymentOrder> optPayment = paymentOrderRepository.findByClientTransactionId("G"+order.getOrderGroupId());
            if (!optPayment.isPresent()) {
                //find individual order
                optPayment = paymentOrderRepository.findByClientTransactionId(order.getId());
            }
            if (!optPayment.isPresent()) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Payment Order not found with orderId: " + orderId);
                response.setErrorStatus(HttpStatus.NOT_FOUND);
                response.setMessage("Payment Order not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            paymentOrder = optPayment.get();
        }

        if (order.getIsRevised()!=null) {
            if (order.getIsRevised()) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order already revised. OrderId: " + orderId);
                response.setErrorStatus(HttpStatus.EXPECTATION_FAILED);
                response.setMessage("Order already revised");
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
            }
        }
        
        StoreWithDetails storeWithDetials = null;
        Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(order.getStoreId());
        if (optStore.isPresent()) {
            storeWithDetials = optStore.get();
        } else {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "store with storeId: " + order.getStoreId() + " not found");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("store not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        if (order.getCompletionStatus()==OrderStatus.PAYMENT_CONFIRMED || order.getCompletionStatus()==OrderStatus.RECEIVED_AT_STORE) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order item preview revise for orderId: " + orderId);
            
            //get original order item
            List<OrderItem> orderNewItemList = new ArrayList<OrderItem>();
            
            for (int i=0;i<bodyOrderItemList.length;i++) {
                OrderItem orderItem = bodyOrderItemList[i];
                Optional<OrderItem> originalOrderItem = orderItemRepository.findById(orderItem.getId());
                if (originalOrderItem.isPresent()) {
                    OrderItem originalItem = originalOrderItem.get();
                    if (orderItem.getQuantity()>originalItem.getQuantity()) {
                        //return error if item to increase quantity
                        response.setMessage("Cannot increase item quantity");
                        response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);            
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
                    }
                    float itemPrice = originalItem.getProductPrice();
                    originalItem.setOriginalQuantity(originalItem.getQuantity());
                    originalItem.setQuantity(orderItem.getQuantity());
                    originalItem.setPrice(orderItem.getQuantity() * (float)itemPrice);
                    orderNewItemList.add(originalItem);
                    //price is already discounted price (if any)
                }
            }
                        
            //save new quantity
            for (int i=0;i<orderNewItemList.size();i++) {
                OrderItem orderNewItem = orderNewItemList.get(i);                
                orderItemRepository.save(orderNewItem);
            }
            
            //get new sales total amount
            double newSalesAmount=0.00;
            List<OrderItem> orderItemList = orderItemRepository.findByOrderId(orderId);
            for (int i=0;i<orderItemList.size();i++) {
                OrderItem orderItem = orderItemList.get(i);
                newSalesAmount = newSalesAmount + orderItem.getPrice();
            }
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "newSalesAmount: " + newSalesAmount);
            
            //get discount from original discount
            double newAppliedDiscount = 0.00;
            if (order.getDiscountId()!=null) {
                String calculationType = order.getDiscountCalculationType();
                double discountTierAmount = order.getDiscountCalculationValue();
                Optional<StoreDiscount> storeDiscountOpt = storeDiscountRepository.findById(order.getDiscountId());
                StoreDiscount storeDiscount = storeDiscountOpt.get();
                String discountType = storeDiscount.getDiscountType();
                if (discountType.equals(DiscountType.TOTALSALES.toString())) {
                    //only recalculate total sales discount
                    double subdiscount=0;
                    if (calculationType.equals(DiscountCalculationType.FIX.toString())) {
                        subdiscount = discountTierAmount;
                    } else if (calculationType.equals(DiscountCalculationType.PERCENT.toString())) {
                        subdiscount = discountTierAmount / 100 * newSalesAmount;
                    }
                    newAppliedDiscount = subdiscount;            
                }
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Discount calculationType:" + calculationType +" discountTierAmount:"+discountTierAmount+" newAppliedDiscount:"+newAppliedDiscount);
            }
            
            double deliveryCharge = 0.00;
            double deliveryDiscount = 0.00;
            if (order.getDeliveryCharges()!=null) {
                deliveryCharge = order.getDeliveryCharges();
            }  
            if (order.getDeliveryDiscount()!=null) {
                deliveryDiscount = order.getDeliveryDiscount();
            } 
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "deliveryCharge:" + deliveryCharge +" deliveryDiscount:"+deliveryDiscount);
            
            //calculate Store service charge 
            double newStoreServiceCharges = calculateStoreServiceCharges(storeWithDetials.getServiceChargesPercentage(), newSalesAmount, newAppliedDiscount);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store serviceCharges: " + newStoreServiceCharges);
            
            //calculate grand total
            double newGrandTotal = newSalesAmount - newAppliedDiscount + newStoreServiceCharges + deliveryCharge - deliveryDiscount;
            double oldGranTotal = order.getTotal();
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "newGrandTotal:" + newGrandTotal +" oldGranTotal:"+oldGranTotal);
            
            //calculating Kalsym commission 
            double newKLCommission = 0;
            StoreCommission storeCommission = productService.getStoreCommissionByStoreId(order.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store commission: " + storeCommission);
            double commission = 0.00;
            if (storeCommission != null) {
                commission = newAppliedDiscount * (storeCommission.getRate() / 100);
                if (commission < storeCommission.getMinChargeAmount()) {
                    commission = storeCommission.getMinChargeAmount();
                }
            }
            newKLCommission = Utilities.Round2DecimalPoint(commission);
            
            //calculate Store share
            double newStoreShare = Utilities.Round2DecimalPoint(newSalesAmount - newAppliedDiscount + newStoreServiceCharges - commission);            
            String deliveryType = order.getDeliveryType();
            if (deliveryType!=null) {
                if (deliveryType.equals(DeliveryType.SELF.name())) {
                    double storeShare = newStoreShare + deliveryCharge;
                    newStoreShare = Utilities.Round2DecimalPoint(storeShare);
                } 
            }
            
            // update order details           
            order.setSubTotal(newSalesAmount);
            order.setAppliedDiscount(newAppliedDiscount);
            order.setStoreServiceCharges(newStoreServiceCharges);
            order.setTotal(newGrandTotal);
            order.setKlCommission(newKLCommission);
            order.setStoreShare(newStoreShare);
            order.setIsRevised(true);            
            order = orderRepository.save(order);
            
            //insert refund record for finance to refund for ONLINEPAYMENT only
            double refundAmount = 0.00;
            if (paymentOrder!=null) {
                refundAmount = oldGranTotal - newGrandTotal ;            
                OrderRefund orderRefund = new OrderRefund();
                orderRefund.setOrderId(order.getId());
                orderRefund.setRefundType(RefundType.ITEM_REVISED);            
                orderRefund.setPaymentChannel(paymentOrder.getPaymentChannel());            
                orderRefund.setRefundAmount(refundAmount);
                orderRefund.setRefundStatus(RefundStatus.PENDING);
                orderRefundRepository.save(orderRefund);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "refund record created for orderId: " + order.getId()+" refundAmount:"+refundAmount);                                
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order is COD. no refund record created for orderId: " + order.getId());
            }
            
            //send email to customer            
            String subject = null;
            String content = null;
            //String[] url = deliveryResponse.data.trackingUrl;
            String receiver = order.getOrderShipmentDetail().getEmail();
            OrderPaymentStatusUpdate orderPaymentStatusUpdate = new OrderPaymentStatusUpdate();
            Body body = new Body();

            body.setCurrency(storeWithDetials.getRegionCountry().getCurrencyCode());
            body.setDeliveryAddress(order.getOrderShipmentDetail().getAddress());
            body.setDeliveryCity(order.getOrderShipmentDetail().getCity());
            body.setOrderStatus(order.getCompletionStatus());
            body.setDeliveryCharges(order.getOrderPaymentDetail().getDeliveryQuotationAmount());
            body.setTotal(order.getTotal());
            body.setInvoiceId(order.getInvoiceId());

            body.setStoreAddress(storeWithDetials.getAddress());
            body.setStoreContact(storeWithDetials.getPhoneNumber());
            body.setLogoUrl(storeWithDetials.getStoreLogoUrl() == null ? "" : storeWithDetials.getStoreLogoUrl());
            body.setStoreName(storeWithDetials.getName());

            //get order items
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

            body.setOrderItems(orderItems);

            Email email = new Email();
            email.setBody(body);
            ArrayList<String> tos = new ArrayList<>();
            tos.add(order.getOrderShipmentDetail().getEmail());
            String[] to = Utilities.convertArrayListToStringArray(tos);
            email.setTo(to);
            email.setFrom(storeWithDetials.getRegionVertical().getSenderEmailAdress());
            email.setFromName(storeWithDetials.getRegionVertical().getSenderEmailName());
            email.setDomain(storeWithDetials.getRegionVertical().getDomain()); 

            OrderCompletionStatusConfig orderCompletionStatusConfig = null;
            List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatus(storeWithDetials.getVerticalCode(), "ITEM_REVISED");            
            if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for status: ITEM_REVISED");                
            } else {        
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size());
                orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);
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
                        Optional<RegionCountry> t = regionCountriesRepository.findById(storeWithDetials.getRegionCountryId());
                        if (t.isPresent()) {
                            regionCountry = t.get();
                        }
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
                        String deliveryChargesRemarks="";
                        if (order.getOrderPaymentDetail().getIsCombinedDelivery()) {
                            List<OrderPaymentDetail> orderPaymentDetailList = orderPaymentDetailRepository.findByDeliveryQuotationReferenceId(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId());
                            deliveryChargesRemarks = " (combined x"+orderPaymentDetailList.size()+" shops)";
                        }
                        emailContent = MessageGenerator.generateEmailContent(emailContent, order, storeWithDetials, orderItems, orderShipmentDetail, paymentDetails, regionCountry, false, null, null, assetServiceBaseUrl, deliveryChargesRemarks, refundAmount);
                        email.setRawBody(emailContent);
                        emailService.sendEmail(email);
                    } catch (Exception ex) {
                        Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error sending email :", ex);
                    }
                } else {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "email content is null");
                }
            }
            
            //check if still got item
            boolean gotItem=false;
            for (int i=0;i<orderItems.size();i++) {
                OrderItem orderItem = orderItems.get(i);
                if (orderItem.getQuantity()>0) {
                    gotItem=true;
                    break;
                }
            }
            if (!gotItem){
                //cancel the order
                boolean res = OrderWorker.ProcessOrder(orderId, "CANCEL", logprefix, processOrderUrl);               
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Cancel order result:"+res);
            }
            
            response.setSuccessStatus(HttpStatus.ACCEPTED);            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } else {
            //not allow to revise
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "current status not allow to revise : " + order.getCompletionStatus());
            response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
            response.setMessage("Order not allow to revise");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }               
    }
    
    @GetMapping(path = {"/pdf/{orderId}"}, name = "orders-get-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> getPdf(HttpServletRequest request,
           @PathVariable String orderId) {
        
        String logprefix = request.getRequestURI() + "getPdf()";
        Optional<Order> optOrder = orderRepository.findById(orderId);
        
        if (!optOrder.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: " + orderId);
            
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("Content-Disposition", "inline; filename="+orderId+".pdf");
        
            return ResponseEntity
                    .ok()
                    .headers(responseHeaders)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(null);
        }
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order found with orderId: " + orderId);
        Order order = optOrder.get();
        List<OrderItem> orderItemList = orderItemRepository.findByOrderId(order.getId());
        OrderShipmentDetail orderShipmentDetail = orderShipmentDetailRepository.findByOrderId(order.getId());
        Optional<StoreWithDetails> storeWithDetails = storeDetailsRepository.findById(order.getStoreId());
        RegionCountry regionCountry = null;
        Optional<RegionCountry> t = regionCountriesRepository.findById(storeWithDetails.get().getRegionCountryId());
        if (t.isPresent()) {
            regionCountry = t.get();
        }
        ByteArrayInputStream bis = GeneratePdfReport.orderInvoice(order, orderItemList, storeWithDetails.get(), orderShipmentDetail, regionCountry, assetServiceBaseUrl, orderPaymentDetailRepository);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Disposition", "inline; filename="+orderId+".pdf");

        return ResponseEntity
                .ok()
                .headers(responseHeaders)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
    
    @GetMapping(path = {"/track/{orderId}"}, name = "order-shipment-details-get")    
    public ResponseEntity<HttpResponse> getTrackingUrl(HttpServletRequest request,
            @PathVariable(required = true) String orderId) throws Exception {
        String logprefix = request.getRequestURI() + " ";

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-shipment-details-get, orderId: {}", orderId);

        Optional<Order> order = orderRepository.findById(orderId);

        if (!order.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-shipment-details-get, orderId, not found. orderId: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        OrderShipmentDetail orderShipment = orderShipmentDetailRepository.findByOrderId(orderId);        
        
        if (orderShipment!=null && orderShipment.getCustomerTrackingUrl()!=null) {            
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("Location", orderShipment.getCustomerTrackingUrl());
            
            return ResponseEntity
                .status(HttpStatus.MOVED_PERMANENTLY)
                .headers(responseHeaders)
                .body(null);
        } else {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Tracking url not found", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
       
        
    }
    
    
    @GetMapping(path = {"/famous/{tagKeyword}"}, name = "order-items-get")
    @PreAuthorize("hasAnyAuthority('order-items-get', 'all')")
    public ResponseEntity<HttpResponse> famousItems(HttpServletRequest request,
            @PathVariable(required = true) String tagKeyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        String logprefix = request.getRequestURI() + " ";

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-items-famous, tagKeyword: " + tagKeyword);
        
        TagKeyword tag = tagRepository.findByKeyword(tagKeyword);
        List<Product> famousProductList = new ArrayList();
        
        if (tag!=null) {                            
            String tagType="foodcourt";
            for (int i=0;i<tag.getTagConfigs().size();i++) {
                TagConfig tagConfig = tag.getTagConfigs().get(i);
                if (tagConfig.getProperty().equals("type")) {
                    tagType = tagConfig.getContent();
                }
            }
            
            int limit =5;
            if (tagType.equalsIgnoreCase("restaurant")) {
               limit=30;
            }

            //get famous product for the store
            for (int x=0;x<tag.getTagDetails().size();x++) {
                TagDetails tagDetails = tag.getTagDetails().get(x);
                String storeId = tagDetails.getStoreId();
                if (storeId!=null) {
                 List<Object[]> productList = orderItemRepository.getFamousItemByStoreId(storeId, limit);
                 for (int z=0;z<productList.size();z++) {
                       Object[] product = productList.get(z);
                       java.math.BigInteger count = (java.math.BigInteger)product[0];
                       String itemCode = (String)product[1];
                       String productId = (String)product[2];
                       String productName = (String)product[3];
                       String seoUrl = (String)product[4];
                       String categoryId = (String)product[5];
                       String thumnailUrl = (String)product[6];
                       Product productInfo = new Product();
                       productInfo.setId(productId);
                       productInfo.setName(productName);
                       productInfo.setSeoUrl(seoUrl);
                       productInfo.setStoreId(storeId);
                       productInfo.setCategoryId(categoryId);
                       productInfo.setThumbnailUrl(thumnailUrl);
                       famousProductList.add(productInfo);
                 }
                }
            }
        } else {
            //find in store
            String storeId = tagKeyword;
            int limit=30;
            List<Object[]> productList = orderItemRepository.getFamousItemByStoreId(storeId, limit);
            for (int z=0;z<productList.size();z++) {
                  Object[] product = productList.get(z);
                  java.math.BigInteger count = (java.math.BigInteger)product[0];
                  String itemCode = (String)product[1];
                  String productId = (String)product[2];
                  String productName = (String)product[3];
                  String seoUrl = (String)product[4];
                  String categoryId = (String)product[5];
                  String thumnailUrl = (String)product[6];
                  Product productInfo = new Product();
                  productInfo.setId(productId);
                  productInfo.setName(productName);
                  productInfo.setSeoUrl(seoUrl);
                  productInfo.setStoreId(storeId);
                  productInfo.setCategoryId(categoryId);
                  productInfo.setThumbnailUrl(thumnailUrl);
                  famousProductList.add(productInfo);
            }
        }

        if (famousProductList.isEmpty()) {
            //query tag product feature
            if (tag!=null && tag.getProductFeatureList()!=null && !tag.getProductFeatureList().isEmpty()) {
                for (int x=0;x<tag.getProductFeatureList().size();x++) {
                    famousProductList.add(tag.getProductFeatureList().get(x).getProduct());
                }
            }
        }
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(famousProductList);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    /**
     *
     * @param request
     * @param id
     * @param bodyOrder
     * @return
     */
    
    /**
     * 
     * NOT DOING FOR NOW, KEEP FOR FUTUTRE
     * 
    @PutMapping(path = {"/previewreviseitem/{orderId}"}, name = "orders-put-by-id")
    //@PreAuthorize("hasAnyAuthority('orders-put-by-id', 'all') and @customOwnerVerifier.VerifyOrder(#id)")
    public ResponseEntity<HttpResponse> previewReviseOrderItems(HttpServletRequest request,
            @PathVariable String orderId,
            @Valid @RequestBody OrderItem[] bodyOrderItemList) {
        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "previewReviseOrderItems()", "");
        
        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: " + orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            response.setMessage("Order not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        Optional<PaymentOrder> optPayment = paymentOrderRepository.findByClientTransactionId(orderId);

        if (!optPayment.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Payment Order not found with orderId: " + orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            response.setMessage("Payment Order not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order found with orderId: " + orderId);
        Order order = optOrder.get();
        
        if (order.getCompletionStatus()==OrderStatus.PAYMENT_CONFIRMED || order.getCompletionStatus()==OrderStatus.RECEIVED_AT_STORE) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order item preview revise for orderId: " + orderId);
            for (int i=0;i<bodyOrderItemList.length;i++) {
                OrderItem orderItem = bodyOrderItemList[i];
                if (orderItem.getStatus().equals("CANCELED")) {
                    //cancel item
                    TODO : preview cancel item
                } else if (orderItem.getStatus().equals("REVISED")) {
                    //revise item
                    TODO : preview revise item
                }
            }
            
            TODO : calculate new order total
            Order orderPreview = new Order();
            orderPreview.setAppliedDiscount(Double.MIN_VALUE);
            
            response.setSuccessStatus(HttpStatus.ACCEPTED);            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } else {
            //not allow to cancel
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "current status not allow to revise : " + order.getCompletionStatus());
            response.setSuccessStatus(HttpStatus.CONFLICT);
            response.setMessage("Order not allow to revise");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }               
    }
    **/
    
     /**
     *
     * @param request
     * @param id
     * @param bodyOrder
     * @return
     */
    /**
    @PutMapping(path = {"/confirmreviseitem/{id}"}, name = "orders-put-by-id")
    //@PreAuthorize("hasAnyAuthority('orders-put-by-id', 'all') and @customOwnerVerifier.VerifyOrder(#id)")
    public ResponseEntity<HttpResponse> confirmReviseOrderItems(HttpServletRequest request,
            @PathVariable String id,
            @Valid @RequestBody OrderItem[] bodyOrderItem) {
        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());
           
    }
    * */
    
 
    private CartItem addItemToCart(String storeId, String productId, 
            String itemCode, int quantity, 
            String cartId,
            ServiceType serviceType,
            String specialInstruction) {
        
        String logprefix = "addItemToCart()";
        CartItem bodyCartItem = new CartItem();
        bodyCartItem.setCartId(cartId);
        bodyCartItem.setProductId(productId);
        bodyCartItem.setItemCode(itemCode);
        bodyCartItem.setQuantity(quantity);
        bodyCartItem.setSpecialInstruction(specialInstruction);
        
        CartItem createdCartItem = new CartItem();
        
        //get product info
        Optional<Product> optProduct = productRepository.findById(productId);
        if (!optProduct.isPresent()) {
            bodyCartItem.setCreateStatus(HttpStatus.CONFLICT);
            return bodyCartItem;  
        }
        
        ProductInventory productInventory = productService.getProductInventoryById(storeId, productId, itemCode);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got product inventory details: " + productInventory.toString());
        
            //check if enable product inventory
            if (productInventory.getQuantity()<quantity && optProduct.get().isAllowOutOfStockPurchases()==false) {
                //out of stock
                bodyCartItem.setCreateStatus(HttpStatus.CONFLICT);
                return bodyCartItem;                
            }
                    
            //check for discount
            double itemPrice = 0.00;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "serviceType:"+serviceType+" itemDiscount:"+productInventory.getItemDiscount());
            if (productInventory.getItemDiscount()!=null && serviceType==ServiceType.DELIVERIN) {
                if (productInventory.getItemDiscount().discountAmount>0) {
                    //got discount
                    ItemDiscount discountDetails = productInventory.getItemDiscount();
                    itemPrice = discountDetails.discountedPrice;
                    bodyCartItem.setDiscountId(discountDetails.discountId);
                    bodyCartItem.setNormalPrice((float)discountDetails.normalPrice);
                    bodyCartItem.setDiscountLabel(discountDetails.discountLabel);
                }
            } else if (serviceType==ServiceType.DELIVERIN) {
                //no dicount for this item code
                itemPrice = productInventory.getPrice();
            } else if (productInventory.getItemDiscount()!=null && serviceType==ServiceType.DINEIN) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "dineInDiscountAmount:"+productInventory.getItemDiscount().dineInDiscountAmount);                    
                if (productInventory.getItemDiscount().dineInDiscountAmount>0) {
                    //got discount
                    ItemDiscount discountDetails = productInventory.getItemDiscount();
                    itemPrice = discountDetails.dineInDiscountedPrice;
                    bodyCartItem.setDiscountId(discountDetails.discountId);
                    bodyCartItem.setNormalPrice((float)discountDetails.dineInNormalPrice);
                    bodyCartItem.setDiscountLabel(discountDetails.discountLabel);
                } else {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "No discount for DineIn");
                }
            } else {
                //use dine-in price
                itemPrice = productInventory.getDineInPrice();
            }
            
            bodyCartItem.setProductPrice((float)itemPrice);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "itemPrice:"+itemPrice);
            //check if product is package            
            boolean isPackage = optProduct.get().getIsPackage();            
            if (isPackage) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Product is package");
                bodyCartItem.setPrice(bodyCartItem.getQuantity() * bodyCartItem.getProductPrice());
                bodyCartItem.setProductName(productInventory.getProduct().getName());
                bodyCartItem.setSKU(productInventory.getSKU());
                createdCartItem = cartItemRepository.save(bodyCartItem);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Saved cartItem id:"+createdCartItem.getId());
                //save sub cart item
                if (bodyCartItem.getCartSubItem()!=null) {
                    for (int i=0;i<bodyCartItem.getCartSubItem().size();i++) {
                        CartSubItem subItem = bodyCartItem.getCartSubItem().get(i);
                        subItem.setCartItemId(createdCartItem.getId());
                        cartSubItemRepository.save(subItem);
                    }
                }
            } else {
                
                boolean gotAddOn=false;
                if (bodyCartItem.getCartItemAddOn()!=null && !bodyCartItem.getCartItemAddOn().isEmpty() ) {
                    gotAddOn=true;
                }
                
                CartItem existingItem = null;
                if (gotAddOn==false) {
                    //find item in current cart & no add-on product, increase quantity if already exist
                    existingItem = cartItemRepository.findByCartIdAndItemCodeAndSpecialInstruction(bodyCartItem.getCartId(), bodyCartItem.getItemCode(), bodyCartItem.getSpecialInstruction());
                }
                
                if (existingItem != null) {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "item already exist for cartId: " + bodyCartItem.getCartId());
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "item already exist for itemCode: " + bodyCartItem.getItemCode());

                    int newQty = existingItem.getQuantity() + bodyCartItem.getQuantity();
                    existingItem.setQuantity(newQty);
                    existingItem.setPrice(newQty * existingItem.getProductPrice());
                    existingItem.setProductName(productInventory.getProduct().getName());
                    createdCartItem = cartItemRepository.save(existingItem);
                } else {
                    bodyCartItem.setPrice(bodyCartItem.getQuantity() * bodyCartItem.getProductPrice());
                    bodyCartItem.setProductName(productInventory.getProduct().getName());
                    bodyCartItem.setSKU(productInventory.getSKU());
                    createdCartItem = cartItemRepository.save(bodyCartItem);
                    
                    //save add-on if any
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Item addOn: " + bodyCartItem.getCartItemAddOn());
                    if (bodyCartItem.getCartItemAddOn()!=null && !bodyCartItem.getCartItemAddOn().isEmpty()) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Save cart item addOn: " + bodyCartItem.getCartItemAddOn().toString());
                        for (int x=0;x<bodyCartItem.getCartItemAddOn().size();x++) {
                            CartItemAddOn cartItemAddOn = bodyCartItem.getCartItemAddOn().get(x);
                            cartItemAddOn.setCartItemId(createdCartItem.getId());
                            //get add-on price
                            Optional<ProductAddOn> productAddOnOpt = productAddOnRepository.findById(cartItemAddOn.getProductAddOnId());
                            if (productAddOnOpt.isPresent()) {
                                if (serviceType!=null && serviceType==ServiceType.DINEIN){
                                    cartItemAddOn.setPrice(productAddOnOpt.get().getDineInPrice().floatValue() * bodyCartItem.getQuantity());
                                    cartItemAddOn.setProductPrice(productAddOnOpt.get().getDineInPrice().floatValue());
                                } else {
                                    cartItemAddOn.setPrice(productAddOnOpt.get().getPrice().floatValue() * bodyCartItem.getQuantity());
                                    cartItemAddOn.setProductPrice(productAddOnOpt.get().getPrice().floatValue());
                                }
                            }
                            cartItemAddOnRepository.save(cartItemAddOn);
                        }
                    }
                } 
            }
            return createdCartItem;
            
    }
}

