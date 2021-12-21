package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.enums.ProductStatus;
import com.kalsym.order.service.enums.StorePaymentType;
import com.kalsym.order.service.enums.DeliveryType;
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
import com.kalsym.order.service.model.COD;
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.CartSubItem;
import com.kalsym.order.service.model.ProductInventory;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.StoreDeliveryDetail;
import com.kalsym.order.service.model.Cart;
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
import com.kalsym.order.service.model.ProductInventoryItem;
import com.kalsym.order.service.model.ProductVariantAvailable;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.object.Discount;
import com.kalsym.order.service.model.object.OrderObject;
import com.kalsym.order.service.model.object.OrderDetails;
import com.kalsym.order.service.model.object.ItemDiscount;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.OrderSubItemRepository;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.CartRepository;
import com.kalsym.order.service.model.repository.OrderCompletionStatusConfigRepository;
import com.kalsym.order.service.model.repository.OrderCompletionStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderPaymentDetailRepository;
import com.kalsym.order.service.model.repository.OrderPaymentStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.model.repository.ProductRepository;
import com.kalsym.order.service.model.repository.StoreRepository;
import com.kalsym.order.service.model.repository.OrderShipmentDetailRepository;
import com.kalsym.order.service.model.repository.ProductInventoryRepository;
import com.kalsym.order.service.model.repository.RegionCountriesRepository;
import com.kalsym.order.service.model.repository.StoreDetailsRepository;
import com.kalsym.order.service.model.repository.StoreDiscountRepository;
import com.kalsym.order.service.model.repository.StoreDiscountTierRepository;
import com.kalsym.order.service.model.repository.StoreDeliveryDetailRepository;
import com.kalsym.order.service.service.CustomerService;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.service.FCMService;
import com.kalsym.order.service.service.OrderPostService;
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.MessageGenerator;
import com.kalsym.order.service.utility.OrderCalculation;
import com.kalsym.order.service.utility.StoreDiscountCalculation;
import com.kalsym.order.service.utility.TxIdUtil;
import com.kalsym.order.service.utility.Utilities;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    ProductRepository productRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderSubItemRepository orderSubItemRepository;
    
    @Autowired
    CartItemRepository cartItemRepository;

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
    
    @Value("${onboarding.order.URL:https://symplified.biz/orders/order-details?orderId=}")
    private String onboardingOrderLink;
     
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
            @RequestParam(required = false) OrderStatus[] completionStatus,
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
        
            //get current status
            String currentStatus = order.getCompletionStatus().name();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Find config for current status VerticalId:"+verticalId+" Status:"+currentStatus+" storePickup:"+storePickup+" deliveryType:"+storeDeliveryType+" paymentType:"+order.getPaymentType());
            List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, currentStatus, storePickup, storeDeliveryType, order.getPaymentType());
            OrderCompletionStatusConfig orderCompletionStatusConfig = null;
            if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for current status: " + currentStatus);            
            } else {        
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size());
                orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);

                //get next action
                OrderCompletionStatusConfig nextCompletionStatusConfig = null;
                int nextSequence = orderCompletionStatusConfig.getStatusSequence()+1;
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Find config for next status VerticalId:"+verticalId+" NextSequence:"+nextSequence+" storePickup:"+storePickup+" deliveryType:"+storeDeliveryType+" paymentType:"+order.getPaymentType());
                List<OrderCompletionStatusConfig> nextActionCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusSequenceAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, nextSequence, storePickup, storeDeliveryType, order.getPaymentType());
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

        Order orderMatch = new Order();
        orderMatch.setId(id);

//        ExampleMatcher matcher = ExampleMatcher
//                .matchingAll()
//                .withIgnoreCase()
//                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
//        Example<Order> orderExample = Example.of(orderMatch, matcher);
//        Pageable pageable = PageRequest.of(page, pageSize);
        Optional<Order> optOrder = orderRepository.findById(id);
        if (!optOrder.isPresent()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("order with id " + id + " not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(optOrder.get());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //TODO: add save customer info request parameter
    @PostMapping(path = {""}, name = "orders-post")
    @PreAuthorize("hasAnyAuthority('orders-post', 'all')")
    public ResponseEntity<HttpResponse> postOrders(HttpServletRequest request,
            @Valid @RequestBody Order order,
            @RequestParam(required = false) Boolean saveCustomerInformation) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orders-post request on url: " + request.getRequestURI());

//        OrderObject bodyOrder = new OrderObject();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, order.toString(), "");

        try {

            Optional<Store> optStore = storeRepository.findById(order.getStoreId());

            if (!optStore.isPresent()) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("store not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Optional<StoreDeliveryDetail> optStoreDeliveryDetail = storeDeliveryDetailRepository.findByStoreId(order.getStoreId());

            if (!optStoreDeliveryDetail.isPresent()) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("store delivery detail not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Store store = optStore.get();
            order.setPaymentType(store.getPaymentType());

            StoreCommission storeCommission = productService.getStoreCommissionByStoreId(store.getId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store commission: " + storeCommission);
//            while (true) {
            try {

                String invoiceId = TxIdUtil.generateInvoiceId(store.getId(), store.getNameAbreviation(), storeRepository);
                order.setInvoiceId(invoiceId);
                OrderPaymentDetail opd = order.getOrderPaymentDetail();
                OrderShipmentDetail osd = order.getOrderShipmentDetail();

                order.setDeliveryCharges(opd.getDeliveryQuotationAmount());
                order.setOrderPaymentDetail(null);
                order.setOrderShipmentDetail(null);
                order.setCompletionStatus(OrderStatus.RECEIVED_AT_STORE);
                order.setPaymentStatus(PaymentStatus.PENDING);

                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "serviceChargesPercentage: " + store.getServiceChargesPercentage());
                
                //calculate Store discount
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Validate cartId: " + order.getCartId());
                Optional<Cart> optCart = cartRepository.findById(order.getCartId());
                if (!optCart.isPresent()) {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart with id " + order.getCartId() + " not found");
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                    response.setMessage("cart with id " + order.getCartId() + " not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                } 
                
                Cart cart = optCart.get();
                OrderObject orderTotalObject = OrderCalculation.CalculateOrderTotal(cart, order, store.getServiceChargesPercentage(), storeCommission, cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix);                
                order.setSubTotal(orderTotalObject.getSubTotal());
                order.setAppliedDiscount(orderTotalObject.getAppliedDiscount());
                order.setAppliedDiscountDescription(orderTotalObject.getAppliedDiscountDescription());
                order.setDeliveryDiscount(orderTotalObject.getDeliveryDiscount());
                order.setDeliveryDiscountDescription(orderTotalObject.getDeliveryDiscountDescription());                
                order.setStoreServiceCharges(orderTotalObject.getStoreServiceCharge());
                order.setTotal(orderTotalObject.getTotal());
                order.setKlCommission(orderTotalObject.getKlCommission());
                order.setStoreShare(orderTotalObject.getStoreShare());
                order.setDeliveryCharges(order.getDeliveryCharges());  
                order.setDeliveryType(optStoreDeliveryDetail.get().getType());
                order = orderRepository.save(order);
                
                opd.setOrderId(order.getId());
                osd.setOrderId(order.getId());
                orderPaymentDetailRepository.save(opd);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order payment details created for orderId:" + order.getId());
                orderShipmentDetailRepository.save(osd);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderShipmentDetail created for orderId: " + order.getId());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "create customer from OrderShipmentDetails");

                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order customer Id: " + order.getCustomerId());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order check: " + "undefined".equalsIgnoreCase(order.getCustomerId()));

                //TODO: Uncomment this and fix
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "saveCustomerInformation: " + saveCustomerInformation);

                if (saveCustomerInformation != null && saveCustomerInformation == true) {
                    if (order.getCustomerId() == null || "undefined".equalsIgnoreCase(order.getCustomerId())) {
                        String customerId = customerService.addCustomer(osd, order.getStoreId());

                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "customerId: " + customerId);

                        if (customerId != null) {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "customer created with id: " + customerId);
                            order.setCustomerId(customerId);
                            orderRepository.save(order);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "added customerId: " + customerId + " to order: " + order.getId());

                        }
                    } else {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "customer already created with id: " + order.getCustomerId());
                        String customerId = customerService.updateCustomer(osd, order.getStoreId(), order.getCustomerId());
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "updated customer information for id: " + customerId);

                    }
                } else {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "user information not saved");
                }

            } catch (Exception ex) {
                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "exception occure while storing order ", ex);
                response.setMessage(ex.getMessage());
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
            }
//            }

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order created with id: " + order.getId());

            //inserting ordercompleting statusupdate  to pending
            OrderCompletionStatusUpdate orderCompletionStatusUpdate = new OrderCompletionStatusUpdate();
            orderCompletionStatusUpdate.setOrderId(order.getId());
            orderCompletionStatusUpdate.setStatus(OrderStatus.RECEIVED_AT_STORE);
            orderCompletionStatusUpdateRepository.save(orderCompletionStatusUpdate);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "update order status:" + orderCompletionStatusUpdate.getStatus().toString());

            //inserting paymentstatusupdate
            OrderPaymentStatusUpdate orderPaymentStatusUpdate = new OrderPaymentStatusUpdate();
            orderPaymentStatusUpdate.setOrderId(order.getId());
            orderPaymentStatusUpdate.setStatus(PaymentStatus.PENDING);
            orderPaymentStatusUpdateRepository.save(orderPaymentStatusUpdate);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "update order status: " + orderPaymentStatusUpdate.getStatus().toString());

//            //clear cart item
//            cartItemRepository.clearCartItem(cart.getCartId());
            // pass orderId to OrderPostService, even though the status is not completed yet
            //orderPostService.postOrderLink(cart.getId(), cart.getStoreId());
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving order", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }

        //Optional<Order> orderDetails = orderRepository.findById(savedOrder.getId());
        response.setSuccessStatus(HttpStatus.CREATED);
        response.setData(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     *
     *
     * @param request
     * @param cartId
     * @param orderId
     * @param cod
     * @return
     * @throws Exception
     */
    @PostMapping(path = {"/placeOrder"}, name = "orders-push-cod")
    @PreAuthorize("hasAnyAuthority('orders-push-cod', 'all')")
    public ResponseEntity<HttpResponse> pushCODOrder(HttpServletRequest request,
            @RequestParam(required = true) String cartId,  
            @RequestParam(required = false) Boolean saveCustomerInformation,
            @RequestBody COD cod) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orders-push-cod request on url: " + request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orders-push-cod request body: " + cod.toString());

        // create order object
        Order order = new Order();
        try {
            Optional<Cart> optCart = cartRepository.findById(cartId);
            if (!optCart.isPresent()) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart with id " + cartId + " not found");
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("cart with id " + cartId + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Cart cart = optCart.get();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart exists against cartId: " + cartId);

            //getting store details for cart if from product service
            StoreWithDetails storeWithDetials = productService.getStoreById(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store details of cartId: " + cartId + ", and storeId: " + cart.getStoreId());

            if (storeWithDetials == null) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "store with storeId: " + cart.getStoreId() + " not found");
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("store not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Optional<StoreDeliveryDetail> optStoreDeliveryDetail = storeDeliveryDetailRepository.findByStoreId(cart.getStoreId());

            if (!optStoreDeliveryDetail.isPresent()) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("store delivery detail not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            //getting store details 
            StoreDeliveryDetail storeDeliveryDetail = productService.getStoreDeliveryDetails(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store details: " + storeDeliveryDetail.toString());

            StoreCommission storeCommission = productService.getStoreCommissionByStoreId(cart.getStoreId());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store commission: " + storeCommission);

            Double subTotal = 0.0;
            List<OrderItem> orderItems = new ArrayList<OrderItem>();
            try {
                // check store payment type
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store with payment type: " + storeWithDetials.getPaymentType());

                order.setStoreId(storeWithDetials.getId());
                    
                // get cart items 
                List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got cartItems of cartId: " + cartId + ", items: " + cartItems.toString());

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
                    
                    double itemPrice=0.00;
                    
                    //check for discounted item
                    if (cartItems.get(i).getDiscountId()!=null) {
                        //check if discount still valid
                        ItemDiscount discountDetails = productInventory.getItemDiscount();
                        if (discountDetails.discountId.equals(cartItems.get(i).getDiscountId()) &&
                                discountDetails.discountedPrice==cartItems.get(i).getPrice()) {
                            //dicount still valid
                            subTotal += discountDetails.discountedPrice;
                            itemPrice = discountDetails.discountedPrice;
                        } else {
                            //discount no more valid
                            // should return warning if prices are not same
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Discount not valid");
                            response.setSuccessStatus(HttpStatus.CONFLICT);
                            response.setMessage("Discount not valid");
                            response.setData(cartItems.get(i));
                            return ResponseEntity.status(HttpStatus.CREATED).body(response);
                        }
                    } else {                    
                        if (cartItems.get(i).getProductPrice() != Float.parseFloat(String.valueOf(productInventory.getPrice()))) {
                            // should return warning if prices are not same
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "prices are not same, price got : oldPrice: " + cartItems.get(i).getProductPrice() + ", newPrice: " + String.valueOf(productInventory.getPrice()));
                            response.setSuccessStatus(HttpStatus.CONFLICT);
                            response.setMessage("Conflict in prices, please update prices in cartItems, oldPrice: " + cartItems.get(i).getProductPrice() + ", newPrice: " + String.valueOf(productInventory.getPrice()));
                            response.setData(cartItems.get(i));
                            return ResponseEntity.status(HttpStatus.CREATED).body(response);
                        }
                        subTotal += productInventory.getPrice();
                        itemPrice = productInventory.getPrice();
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
                    }
                    orderPostService.postOrderLink(order.getId(), order.getStoreId(), orderItems);


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
                        
                    //adding new orderItem to orderItems list
                    orderItems.add(orderItem);

                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "added orderItem to order list: " + orderItem.toString());
                }

                order.setCartId(cartId);                    
                order.setCompletionStatus(OrderStatus.RECEIVED_AT_STORE);
                order.setPaymentStatus(PaymentStatus.PENDING);
                order.setCustomerId(cod.getCustomerId());
                order.setDeliveryCharges(cod.getOrderPaymentDetails().getDeliveryQuotationAmount());
                order.setPaymentType(storeWithDetials.getPaymentType());                
                order.setCustomerNotes(cod.getCustomerNotes());

                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "serviceChargesPercentage: " + storeWithDetials.getServiceChargesPercentage());

                // setting invoice id
                String invoiceId = TxIdUtil.generateInvoiceId(storeWithDetials.getId(), storeWithDetials.getNameAbreviation(), storeRepository);
                order.setInvoiceId(invoiceId);

                // setting this empty
                order.setPrivateAdminNotes("");

                OrderObject orderTotalObject = OrderCalculation.CalculateOrderTotal(cart, order, storeWithDetials.getServiceChargesPercentage(), storeCommission, cartItemRepository, storeDiscountRepository, storeDiscountTierRepository, logprefix);                
                order.setSubTotal(orderTotalObject.getSubTotal());
                order.setAppliedDiscount(orderTotalObject.getAppliedDiscount());
                order.setAppliedDiscountDescription(orderTotalObject.getAppliedDiscountDescription());
                order.setDeliveryDiscount(orderTotalObject.getDeliveryDiscount());
                order.setDeliveryDiscountDescription(orderTotalObject.getDeliveryDiscountDescription());                
                order.setStoreServiceCharges(orderTotalObject.getStoreServiceCharge());
                order.setTotal(orderTotalObject.getTotal());
                order.setKlCommission(orderTotalObject.getKlCommission());
                order.setStoreShare(orderTotalObject.getStoreShare());
                order.setDeliveryCharges(order.getDeliveryCharges());
                
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
                
                if (storePickup==null) {
                    storePickup=false;
                    cod.getOrderShipmentDetails().setStorePickup(false);
                    order.setDeliveryType(optStoreDeliveryDetail.get().getType());
                } else {
                     if (storePickup)
                        order.setDeliveryType(DeliveryType.PICKUP.name());
                     else
                        order.setDeliveryType(optStoreDeliveryDetail.get().getType());
                }
                cod.getOrderShipmentDetails().setOrderId(order.getId());
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
                    
                //clear cart item for COD. for online payment only clear after payment confirmed
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order Payment Type:"+order.getPaymentType());
                if (order.getPaymentType().equals(StorePaymentType.COD.name())) {
                    cartItemRepository.clearCartItem(cart.getId());
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Cart item cleared for cartId:"+cart.getId());
                }

                if (saveCustomerInformation != null && saveCustomerInformation == true) {
                    if (order.getCustomerId() == null || "undefined".equalsIgnoreCase(order.getCustomerId())) {
                        String customerId = customerService.addCustomer(cod.getOrderShipmentDetails(), order.getStoreId());

                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "customerId: " + customerId);

                        if (customerId != null) {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "customer created with id: " + customerId);
                            order.setCustomerId(customerId);
                            orderRepository.save(order);
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "added customerId: " + customerId + " to order: " + order.getId());

                        }
                    } else {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "customer already created with id: " + order.getCustomerId());
                        String customerId = customerService.updateCustomer(cod.getOrderShipmentDetails(), order.getStoreId(), order.getCustomerId());
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "updated customer information for id: " + customerId);

                    }
                } else {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "user information not saved");
                }
                    
                //get order completion config
                String verticalId = storeWithDetials.getVerticalCode();                    
                String storeDeliveryType = storeDeliveryDetail.getType();
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status:"+OrderStatus.RECEIVED_AT_STORE.name()+" VerticalId:"+verticalId+" storePickup:"+storePickup+" deliveryType:"+storeDeliveryType+" paymentType:"+storeWithDetials.getPaymentType());
                List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, OrderStatus.RECEIVED_AT_STORE.name(), storePickup, storeDeliveryType, storeWithDetials.getPaymentType());
                OrderCompletionStatusConfig orderCompletionStatusConfig = null;
                if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
                    Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for status: " + OrderStatus.RECEIVED_AT_STORE.name());             
                } else {        
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size());
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
                                emailContent = MessageGenerator.generateEmailContent(emailContent, order, storeWithDetials, orderItems, order.getOrderShipmentDetail(), regionCountry);
                                Email email = new Email();
                                ArrayList<String> tos = new ArrayList<>();
                                tos.add(order.getOrderShipmentDetail().getEmail());
                                String[] to = Utilities.convertArrayListToStringArray(tos);
                                email.setTo(to);
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
                                body.setLogoUrl(storeWithDetials.getStoreAsset() == null ? "" : storeWithDetials.getStoreAsset().getLogoUrl());
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
                            fcmService.sendPushNotification(order, storeWithDetials.getId(), storeWithDetials.getName(), pushNotificationTitle, pushNotificationContent, OrderStatus.RECEIVED_AT_STORE);
                        } catch (Exception e) {
                            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "pushNotificationToMerchat error ", e);
                        }

                    }
                }
               
            } catch (Exception ex) {
                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "exception occur while creating order ", ex);
                response.setMessage(ex.getMessage());
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
            }

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Everything is fine thanks for using this API for placing order");
            response.setSuccessStatus(HttpStatus.CREATED);
            response.setData(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            //orderPostService.postOrderLink(cart.getId(), cart.getStoreId());
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving order", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }

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

//    /**
//     *
//     * @param request
//     * @param id
//     * @param bodyOrder
//     * @return
//     */
//    @PostMapping(path = {"/update/{id}"}, name = "orders-update-by-id")
//    @PreAuthorize("hasAnyAuthority('orders-update-by-id', 'all')")
//    public ResponseEntity<HttpResponse> updateOrdersById(HttpServletRequest request,
//            @PathVariable String id,
//            @RequestBody OrderObject bodyOrder) {
//        String logprefix = request.getRequestURI() + " ";
//        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
//        HttpResponse response = new HttpResponse(request.getRequestURI());
//
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "", "");
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrder.toString(), "");
//
//        Optional<Order> optCart = orderRepository.findById(id);
//
//        if (!optCart.isPresent()) {
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: {}", id);
//            response.setErrorStatus(HttpStatus.NOT_FOUND);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart found with orderId: {}", id);
//        Order cart = optCart.get();
//
//        if (bodyOrder.getPaymentStatus().equalsIgnoreCase("SUCCESS")) {
//            cart.setCompletionStatus("Received");
//            cart.setPaymentStatus("Completed");
//            orderPostService.postOrderLink(cart.getId(), bodyOrder.getStoreId());
//
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart success for orderId: {}", id);
//            //check if need adhoc delivery        
//            List<OrderItem> itemList = orderItemRepository.findByOrderId(cart.getId());
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderId:{} itemList size:{}", cart.getId(), itemList.size());
//            if (itemList.size() > 0) {
//                Optional<Product> product = productRepository.findById(itemList.get(0).getProductId());
//                if (product.isPresent()) {
//                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderId:{} Product found:{} deliveryType:{}", cart.getId(), product.get().getId(), product.get().getDeliveryType());
//                    if (product.get().getDeliveryType().equals("ADHOC")) {
//                        // trigger delivery service
//                        DeliveryServiceSubmitOrder deliveryServiceSubmitOrder = new DeliveryServiceSubmitOrder();
//                        OrderShipmentDetail orderShipmentDetail = orderShipmentDetailRepository.findByOrderId(id);
//                        deliveryServiceSubmitOrder.setOrderId(id);
//                        deliveryServiceSubmitOrder.setCustomerId(cart.getCustomerId());
//                        deliveryServiceSubmitOrder.setStoreId(cart.getStoreId());
//                        deliveryServiceSubmitOrder.setPieces(1);
//                        deliveryServiceSubmitOrder.setProductCode("document");
//                        deliveryServiceSubmitOrder.setItemType("parcel");
//                        deliveryServiceSubmitOrder.setTotalWeightKg(1);
//                        deliveryServiceSubmitOrder.setIsInsurance(false);
//                        deliveryServiceSubmitOrder.setShipmentContent("Food");
//                        deliveryServiceSubmitOrder.setShipmentValue(cart.getTotal());
//                        deliveryServiceSubmitOrder.setDeliveryProviderId(orderShipmentDetail.getDeliveryProviderId());
//                        //pickup details
//                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Find storeId:{}", cart.getStoreId());
//                        Optional<Store> fstore = storeRepository.findById(cart.getStoreId());
//                        if (fstore.isPresent()) {
//                            Store store = fstore.get();
//                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "storeId:{} Store found. contactName:{}", cart.getStoreId(), store.getContactName());
//                            DeliveryServicePickupDetails pickupDetails = new DeliveryServicePickupDetails();
//                            pickupDetails.setPickupContactName(store.getContactName());
//                            pickupDetails.setTrolleyRequired(false);
//                            pickupDetails.setPickupContactPhone(store.getPhone());
//                            pickupDetails.setPickupContactEmail(store.getEmail());
//                            pickupDetails.setPickupAddress(store.getAddress());
//                            pickupDetails.setPickupPostcode(store.getPostcode());
//                            pickupDetails.setPickupCity(store.getCity());
//                            pickupDetails.setPickupState(store.getState());
//                            pickupDetails.setPickupOption("ADHOC");
//                            pickupDetails.setVehicleType("MOTORCYCLE");
//                            deliveryServiceSubmitOrder.setPickup(pickupDetails);
//                        }
//                        //delivery details
//                        DeliveryServiceDeliveryDetails deliveryDetails = new DeliveryServiceDeliveryDetails();
//                        deliveryDetails.setDeliveryAddress(orderShipmentDetail.getAddress());
//                        deliveryDetails.setDeliveryCity(orderShipmentDetail.getCity());
//                        deliveryDetails.setDeliveryPostcode(orderShipmentDetail.getZipcode());
//                        deliveryDetails.setDeliveryState(orderShipmentDetail.getState());
//                        deliveryDetails.setDeliveryCountry(orderShipmentDetail.getCountry());
//                        deliveryDetails.setDeliveryContactName(orderShipmentDetail.getReceiverName());
//                        deliveryDetails.setDeliveryContactPhone(orderShipmentDetail.getPhoneNumber());
//                        deliveryDetails.setDeliveryContactEmail(orderShipmentDetail.getEmail());
//                        deliveryServiceSubmitOrder.setDelivery(deliveryDetails);
//                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "submit to delivey-service orderId:{}", cart.getId());
//                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request Body:{}", deliveryServiceSubmitOrder.toString());
//                        DeliveryServiceResponse deliveryResponse = deliveryService.submitDeliveryOrder(deliveryServiceSubmitOrder);
//                        deliveryService.confirmOrderDelivery(cart.getOrderPaymentDetail().getDeliveryQuotationReferenceId());
//                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Response Data isSuccess:{} trackingUrl:{}", deliveryResponse.data.isSuccess, deliveryResponse.data.trackingUrl);
//                        if (deliveryResponse.data.isSuccess) {
//                            cart.setCompletionStatus("ReadyForDelivery");
//                            //send email with tracking url
//                            String[] url = deliveryResponse.data.trackingUrl;
//                            String receiver = orderShipmentDetail.getEmail();
//                            String subject = "[" + cart.getId() + "] Your cart is being deliver";
//                            String content = "Your cart " + cart.getId() + " is being deliver. Use this url to track your cart :"
//                                    + "<br/>";
//                            for (int i = 0; i < url.length; i++) {
//                                content += "<br/>" + url[0];
//                            }
//                            logger.debug("Sending Email! Receiver:" + receiver + " Subject:" + subject + " Content:" + content);
//                            emailService.SendEmail(receiver, subject, content);
//                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Sent Email");
//                            // pass orderId to OrderPostService
//                            logger.debug("Posting cart");
//                            orderPostService.postOrderLink(id, bodyOrder.getStoreId());
//                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order Posted on live chat");
//                        } else {
//                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "adhoc delivery fail for orderId: {}", id);
//                        }
//                    }
//                }
//            }
//        } else {
//            cart.setCompletionStatus("OnHold");
//            cart.setPaymentStatus("Failed");
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "payment fail orderId: {}", id);
//        }
//
//        orderRepository.save(cart);
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cart updated for orderId: {}", id);
//        response.setSuccessStatus(HttpStatus.ACCEPTED);
//        response.setData(cart);
//        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
//    }
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
                    statusPredicatesList.add(predicateForCompletionStatus);
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
    
    
    @GetMapping(path = {"/countsummary/{storeId}"}, name = "orders-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get', 'all') and @customOwnerVerifier.VerifyStore(#storeId)")
    public ResponseEntity<HttpResponse> getCountSummary(HttpServletRequest request,
            @PathVariable(required = true) String storeId
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
        
        List<Object> dataSummaryList = new ArrayList<Object>();
        List<Object[]> countSummaryList = orderRepository.getCountSummary(storeId);
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
        
        //get current status
        String currentStatus = order.getCompletionStatus().name();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Find config for current status VerticalId:"+verticalId+" Status:"+currentStatus+" storePickup:"+storePickup+" deliveryType:"+storeDeliveryType+" paymentType:"+order.getPaymentType());
        List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, currentStatus, storePickup, storeDeliveryType, order.getPaymentType());
        OrderCompletionStatusConfig orderCompletionStatusConfig = null;
        if (orderCompletionStatusConfigs == null || orderCompletionStatusConfigs.isEmpty()) {
            Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Status config not found for current status: " + currentStatus);            
        } else {        
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderStatusstatusConfigs: " + orderCompletionStatusConfigs.size());
            orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);
                
            //get next action
            OrderCompletionStatusConfig nextCompletionStatusConfig = null;
            int nextSequence = orderCompletionStatusConfig.getStatusSequence()+1;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Find config for next status VerticalId:"+verticalId+" NextSequence:"+nextSequence+" storePickup:"+storePickup+" deliveryType:"+storeDeliveryType+" paymentType:"+order.getPaymentType());
            List<OrderCompletionStatusConfig> nextActionCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusSequenceAndStorePickupAndStoreDeliveryTypeAndPaymentType(verticalId, nextSequence, storePickup, storeDeliveryType, order.getPaymentType());
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

}

