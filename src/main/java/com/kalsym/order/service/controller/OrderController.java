package com.kalsym.order.service.controller;

import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.enums.ProductStatus;
import com.kalsym.order.service.enums.StorePaymentType;
import com.kalsym.order.service.model.OrderPaymentDetail;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.kalsym.order.service.model.ProductInventory;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.StoreDeliveryDetail;
import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.Product;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
import com.kalsym.order.service.model.OrderPaymentStatusUpdate;
import com.kalsym.order.service.model.Store;
import com.kalsym.order.service.model.StoreCommission;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.CartRepository;
import com.kalsym.order.service.model.repository.OrderCompletionStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderPaymentDetailRepository;
import com.kalsym.order.service.model.repository.OrderPaymentStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.model.repository.ProductRepository;
import com.kalsym.order.service.model.repository.StoreRepository;
import com.kalsym.order.service.model.repository.OrderShipmentDetailRepository;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.service.OrderPostService;
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.utility.TxIdUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import java.util.Optional;
import javax.persistence.criteria.Predicate;
import org.springframework.data.domain.ExampleMatcher;
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

    private static Logger logger = LoggerFactory.getLogger("application");

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
    ProductRepository productRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

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

    @GetMapping(path = {""}, name = "orders-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get', 'all')")
    public ResponseEntity<HttpResponse> getOrders(HttpServletRequest request,
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        logger.info("before from : " + from + ", to : " + to);
//        to.setDate(to.getDate() + 1);
//        logger.info("after adding 1 day to (todate) from : " + from + ", to : " + to);

        logger.info("orders-get request " + request.getRequestURL());
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

        logger.info("orderMatch: " + orderMatch);

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

        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<Order> orderExample = Example.of(orderMatch, matcher);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("created").descending());

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderRepository.findAll(getSpecWithDatesBetween(from, to, orderExample), pageable));

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

    @PostMapping(path = {""}, name = "orders-post")
    @PreAuthorize("hasAnyAuthority('orders-post', 'all')")
    public ResponseEntity<HttpResponse> postOrders(HttpServletRequest request,
            @Valid @RequestBody Order order) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("orders-post request on url: {}", request.getRequestURI());

//        OrderObject bodyOrder = new OrderObject();
        logger.info(order.toString(), "");

        try {

            Optional<Store> optStore = storeRepository.findById(order.getStoreId());

            if (!optStore.isPresent()) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("store not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Store store = optStore.get();
            StoreCommission storeCommission = productService.getStoreCommissionByStoreId(store.getId());
            logger.info("got store commission : " + storeCommission);
//            while (true) {
            try {
                
                
                
                
                
                String invoiceId = TxIdUtil.generateReferenceId(store.getNameAbreviation());
                order.setInvoiceId(invoiceId);
                OrderPaymentDetail opd = order.getOrderPaymentDetail();
                OrderShipmentDetail osd = order.getOrderShipmentDetail();
                
                
                order.setDeliveryCharges(opd.getDeliveryQuotationAmount());
                order.setOrderPaymentDetail(null);
                order.setOrderShipmentDetail(null);
                order.setCompletionStatus(OrderStatus.RECEIVED_AT_STORE);
                order.setPaymentStatus(PaymentStatus.PENDING);
                

                logger.info("serviceChargesPercentage: " + store.getServiceChargesPercentage());

                double serviceCharges = 0;
                if (null != store.getServiceChargesPercentage()) {
                    serviceCharges = (store.getServiceChargesPercentage() / 100) * order.getSubTotal();
                    logger.info("serviceCharges: " + serviceCharges);

                }

                

                //calculating total amount
                //setting store commission 
                double commission = 0;
                if (storeCommission != null) {
                    commission = order.getTotal() * (storeCommission.getRate() / 100);
                    if (commission < storeCommission.getMinChargeAmount()) {
                        commission = storeCommission.getMinChargeAmount();
                    }
                }

                //setting amount values, dont do it anywhere else
                order.setStoreServiceCharges(serviceCharges);
                order.setTotal(serviceCharges + order.getSubTotal() + order.getDeliveryCharges());
                order.setKlCommission(commission);
                order.setStoreShare(order.getSubTotal() + order.getStoreServiceCharges() - commission);
                order.setDeliveryCharges(order.getDeliveryCharges());

                order = orderRepository.save(order);
                opd.setOrderId(order.getId());
                osd.setOrderId(order.getId());
                orderPaymentDetailRepository.save(opd);
                logger.info("Order payment details created for orderId: {}", order.getId());
                orderShipmentDetailRepository.save(osd);
                logger.info("orderShipmentDetail created for orderId: {}", order.getId());
//                break;
            } catch (Exception ex) {
                logger.error("exception occure while storing order ", ex);
                response.setMessage(ex.getMessage());
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
            }
//            }

            logger.info("Order created with id: {}", order.getId());

            //inserting ordercompleting statusupdate  to pending
            OrderCompletionStatusUpdate orderCompletionStatusUpdate = new OrderCompletionStatusUpdate();
            orderCompletionStatusUpdate.setOrderId(order.getId());
            orderCompletionStatusUpdate.setStatus(OrderStatus.RECEIVED_AT_STORE);
            orderCompletionStatusUpdateRepository.save(orderCompletionStatusUpdate);
            logger.info("Order completion status update inserted for orderid: {}, with status: {}", order.getId(), orderCompletionStatusUpdate.getStatus().toString());

            //inserting paymentstatusupdate
            OrderPaymentStatusUpdate orderPaymentStatusUpdate = new OrderPaymentStatusUpdate();
            orderPaymentStatusUpdate.setOrderId(order.getId());
            orderPaymentStatusUpdate.setStatus(PaymentStatus.PENDING);
            orderPaymentStatusUpdateRepository.save(orderPaymentStatusUpdate);
            logger.info("Order payment status update inserted for orderid: {}, with status: {}", order.getId(), orderPaymentStatusUpdate.getStatus().toString());

//            //clear cart item
//            cartItemRepository.clearCartItem(cart.getCartId());
//            logger.info("clear cartItem for cartId: {}", cart.getCartId());
            // pass orderId to OrderPostService, even though the status is not completed yet
            //orderPostService.postOrderLink(cart.getId(), cart.getStoreId());
        } catch (Exception exp) {
            logger.error("Error saving order", exp);
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
     * @param cod
     * @return
     * @throws Exception
     */
    @PostMapping(path = {"/placeOrder"}, name = "orders-push-cod")
    @PreAuthorize("hasAnyAuthority('orders-push-cod', 'all')")
    public ResponseEntity<HttpResponse> pushCODOrder(HttpServletRequest request,
            @RequestParam(required = true) String cartId,
            @RequestBody COD cod) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("orders-push-cod request on url: {}", request.getRequestURI());

        logger.info("orders-push-cod request body: " + cod.toString());

        // create order object
        Order order = new Order();
        try {
            Optional<Cart> optCart = cartRepository.findById(cartId);
            if (!optCart.isPresent()) {
                logger.info("cart with id " + cartId + " not found");
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("cart with id " + cartId + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Cart cart = optCart.get();
            logger.info("cart exists against cartId: {}", cartId);

            //getting store details for cart if from product service
            StoreWithDetails storeWithDetials = productService.getStoreById(cart.getStoreId());
            logger.info("got store details of cartId: {}, and storeId: {}, {}", cartId, cart.getStoreId(), storeWithDetials.toString());

            if (storeWithDetials == null) {
                logger.info("store with storeId: {} not found", cart.getStoreId());
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("store not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            //getting store details 
            StoreDeliveryDetail storeDeliveryDetail = productService.getStoreDeliveryDetails(cart.getStoreId());
            logger.info("got store details, {}", storeDeliveryDetail.toString());

            StoreCommission storeCommission = productService.getStoreCommissionByStoreId(cart.getStoreId());
            logger.info("got store commission : " + storeCommission);

            Double subTotal = 0.0;
            List<OrderItem> orderItems = new ArrayList<OrderItem>();
            try {
                // check store payment type
                if (storeWithDetials.getPaymentType().equalsIgnoreCase(StorePaymentType.COD.toString())) {
                    logger.info("Store with storeId: {} is COD", cart.getStoreId());
                    // get cart items 
                    List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
                    logger.info("got cartItems of cartId: {}, {}", cartId, cartItems.toString());

                    for (int i = 0; i < cartItems.size(); i++) {
                        // check every items price in product service
                        ProductInventory productInventory = productService.getProductInventoryById(cart.getStoreId(), cartItems.get(i).getProductId(), cartItems.get(i).getItemCode());
                        logger.info("got productinventory against itemcode: {}, {}", cartItems.get(i).getItemCode(), productInventory);
                        if (cartItems.get(i).getProductPrice() != Float.parseFloat(String.valueOf(productInventory.getPrice()))) {
                            // should return warning if prices are not same
                            logger.info("prices are not same, price got updated: oldPrice: {}, newPrice: {}", cartItems.get(i).getProductPrice(), String.valueOf(productInventory.getPrice()));
                            response.setSuccessStatus(HttpStatus.CONFLICT);
                            response.setMessage("Conflict in prices, please update prices in cartItems, oldPrice: " + cartItems.get(i).getProductPrice() + ", newPrice: " + String.valueOf(productInventory.getPrice()));
                            response.setData(cartItems.get(i));
                            return ResponseEntity.status(HttpStatus.CREATED).body(response);
                        }
                        subTotal += productInventory.getPrice();

                        //creating orderItem
                        OrderItem orderItem = new OrderItem();
                        orderItem.setItemCode(cartItems.get(i).getItemCode());
                        orderItem.setProductId(cartItems.get(i).getProductId());
                        orderItem.setProductName((productInventory.getProduct() != null) ? productInventory.getProduct().getName() : "");
                        orderItem.setProductPrice(Float.parseFloat(String.valueOf(productInventory.getPrice())));
                        orderItem.setQuantity(cartItems.get(i).getQuantity());
                        orderItem.setSKU(productInventory.getSKU());
                        orderItem.setSpecialInstruction(cartItems.get(i).getSpecialInstruction());
                        orderItem.setWeight(cartItems.get(i).getWeight());
                        orderItem.setPrice(cartItems.get(i).getQuantity() * Float.parseFloat(String.valueOf(productInventory.getPrice())));

                        //adding new orderItem to orderItems list
                        orderItems.add(orderItem);
                        logger.info("added orderItem to order list: {}", orderItem.toString());
                    }

                    order.setCartId(cartId);
                    order.setStoreId(storeWithDetials.getId());
                    order.setCompletionStatus(OrderStatus.RECEIVED_AT_STORE);
                    order.setPaymentStatus(PaymentStatus.PENDING);
                    order.setCustomerId(cod.getCustomerId());
                    order.setDeliveryCharges(cod.getOrderPaymentDetails().getDeliveryQuotationAmount());

                    //setting service charges
                    order.setSubTotal(subTotal);
                    logger.info("serviceChargesPercentage: " + storeWithDetials.getServiceChargesPercentage());

                    Double serviceChargesPercentage = storeWithDetials.getServiceChargesPercentage();
                    Double serviceCharges = (serviceChargesPercentage * subTotal) / 100;
                    order.setStoreServiceCharges(serviceCharges);

                    //setting total 
                    order.setTotal(subTotal + serviceCharges + order.getDeliveryCharges());
                    // setting invoice id
                    String invoiceId = TxIdUtil.generateReferenceId(storeWithDetials.getNameAbreviation());
                    order.setInvoiceId(invoiceId);

                    // setting this empty
                    order.setPrivateAdminNotes("");
                    order.setCustomerNotes("");

                    //setting store commission 
                    if (storeCommission != null) {
                        double commission = order.getTotal() * (storeCommission.getRate() / 100);
                        order.setKlCommission((commission < storeCommission.getMinChargeAmount()) ? storeCommission.getMinChargeAmount() : commission);
                        order.setStoreShare(order.getTotal() - order.getKlCommission());
                    }

                    // saving order object to get order Id
                    order = orderRepository.save(order);
                    logger.info("order posted successfully orderId: {}", order.getId());
                    // save payment details
                    cod.getOrderPaymentDetails().setOrderId(order.getId());
                    order.setOrderPaymentDetail(orderPaymentDetailRepository.save(cod.getOrderPaymentDetails()));
                    logger.info("order payment details inserted successfully: {}", order.getOrderPaymentDetail().toString());
                    // save shipment detials
                    cod.getOrderShipmentDetails().setOrderId(order.getId());
                    order.setOrderShipmentDetail(orderShipmentDetailRepository.save(cod.getOrderShipmentDetails()));
                    logger.info("order shipment details inserted successfully: {}", order.getOrderShipmentDetail().toString());
                    OrderItem orderItem = null;
                    Product product;
                    ProductInventory productInventory;
                    // inserting order items now
                    for (int i = 0; i < orderItems.size(); i++) {
                        // insert orderItem 
                        orderItems.get(i).setOrderId(order.getId());
                        orderItem = orderItemRepository.save(orderItems.get(i));
                        logger.info("orderItem created with id: {}, orderId: {}", orderItem.getId(), orderItem.getOrderId());
                        // getting product information if product tracking is enabled we will reduce the quantity
                        product = productService.getProductById(order.getStoreId(), orderItems.get(i).getProductId());
                        logger.info("Got product details of orderItem: " + product.toString());
                        if (product.isTrackQuantity()) {
                            logger.info("Product tracking is enable");
                            productInventory = productService.reduceProductInventory(order.getStoreId(), orderItems.get(i).getProductId(), orderItems.get(i).getItemCode(), orderItems.get(i).getQuantity());
                            if (!product.isAllowOutOfStockPurchases() && productInventory.getQuantity() <= 0) {
                                // making this product variant outof stock
                                productInventory = productService.changeProductStatus(order.getStoreId(), orderItems.get(i).getProductId(), orderItems.get(i).getItemCode(), ProductStatus.OUTOFSTOCK);
                                logger.info("this product variant is out of stock now storeId: " + order.getStoreId() + ", productId: " + orderItems.get(i).getProductId() + ", itemCode: " + orderItems.get(i).getItemCode());
                            }

                            if (productInventory.getQuantity() <= product.getMinQuantityForAlarm()) {
                                //sending notification for product is going out of stock
                                //we can send email as well
                                orderPostService.sendMinimumQuantityAlarm(order.getId(), order.getStoreId(), orderItems.get(i), productInventory.getQuantity());
                                logger.info("intimation sent for out of stock product id: " + orderItems.get(i).getProductId() + ", SKU: " + orderItems.get(i).getSKU() + ", Name: " + productInventory.getProduct().getName());
                            }

                        } else {
                            logger.info("Product tracking is not enabled by marchant");
                        }
                    }
                    // push cart to rocket chat
                    orderPostService.postOrderLink(order.getId(), order.getStoreId(), orderItems);
                    logger.info("order pushed to merchant rocket chat orderId: {}", order.getId());
                } else {
                    // throw bad request exception
                    logger.info("You cannot place order through this endpoint: {} because store is not cod", "/orders/carts/" + cartId + "/cod/push");
                    response.setMessage("you cannot post order throgh this endpoint because store is not cod");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

            } catch (Exception ex) {
                logger.error("exception occur while pushing order to rocket chat ", ex);
                response.setMessage(ex.getMessage());
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
            }

            logger.info("Everything is fine thanks for using this API for placing order");
            response.setSuccessStatus(HttpStatus.CREATED);
            response.setData(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            //orderPostService.postOrderLink(cart.getId(), cart.getStoreId());
        } catch (Exception exp) {
            logger.error("Error saving order", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }

    }

    @DeleteMapping(path = {"/{id}"}, name = "orders-delete-by-id")
    @PreAuthorize("hasAnyAuthority('orders-delete-by-id', 'all')")
    public ResponseEntity<HttpResponse> deleteOrdersById(HttpServletRequest request,
            @PathVariable(required = true) String id) {
        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("orders-delete-by-id, orderId: {}", id);

        Optional<Order> optProduct = orderRepository.findById(id);

        if (!optProduct.isPresent()) {
            logger.info("order not found with orderId: {}", id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("order found", "");
        orderRepository.deleteById(id);

        logger.info("order deleted, with orderId: {}", id);
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
    @PreAuthorize("hasAnyAuthority('orders-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> putOrdersById(HttpServletRequest request,
            @PathVariable String id,
            @RequestBody Order bodyOrder) {
        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("", "");
        logger.info(bodyOrder.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(id);

        if (!optOrder.isPresent()) {
            logger.info("Order not found with orderId: {}", id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("order found with orderId: {}", id);
        Order order = optOrder.get();
        List<String> errors = new ArrayList<>();

        order.update(bodyOrder);

        logger.info("order updated for orderId: {}", id);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderRepository.save(order));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
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
//        logger.info("", "");
//        logger.info(bodyOrder.toString(), "");
//
//        Optional<Order> optCart = orderRepository.findById(id);
//
//        if (!optCart.isPresent()) {
//            logger.info("Order not found with orderId: {}", id);
//            response.setErrorStatus(HttpStatus.NOT_FOUND);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//
//        logger.info("cart found with orderId: {}", id);
//        Order cart = optCart.get();
//
//        if (bodyOrder.getPaymentStatus().equalsIgnoreCase("SUCCESS")) {
//            cart.setCompletionStatus("Received");
//            cart.setPaymentStatus("Completed");
//            orderPostService.postOrderLink(cart.getId(), bodyOrder.getStoreId());
//
//            logger.info("cart success for orderId: {}", id);
//            //check if need adhoc delivery        
//            List<OrderItem> itemList = orderItemRepository.findByOrderId(cart.getId());
//            logger.info("orderId:{} itemList size:{}", cart.getId(), itemList.size());
//            if (itemList.size() > 0) {
//                Optional<Product> product = productRepository.findById(itemList.get(0).getProductId());
//                if (product.isPresent()) {
//                    logger.info("orderId:{} Product found:{} deliveryType:{}", cart.getId(), product.get().getId(), product.get().getDeliveryType());
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
//                        logger.info("Find storeId:{}", cart.getStoreId());
//                        Optional<Store> fstore = storeRepository.findById(cart.getStoreId());
//                        if (fstore.isPresent()) {
//                            Store store = fstore.get();
//                            logger.info("storeId:{} Store found. contactName:{}", cart.getStoreId(), store.getContactName());
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
//                        logger.info("submit to delivey-service orderId:{}", cart.getId());
//                        logger.info("Request Body:{}", deliveryServiceSubmitOrder.toString());
//                        DeliveryServiceResponse deliveryResponse = deliveryService.submitDeliveryOrder(deliveryServiceSubmitOrder);
//                        deliveryService.confirmOrderDelivery(cart.getOrderPaymentDetail().getDeliveryQuotationReferenceId());
//                        logger.info("Response Data isSuccess:{} trackingUrl:{}", deliveryResponse.data.isSuccess, deliveryResponse.data.trackingUrl);
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
//                            logger.info("Sent Email");
//                            // pass orderId to OrderPostService
//                            logger.debug("Posting cart");
//                            orderPostService.postOrderLink(id, bodyOrder.getStoreId());
//                            logger.info("Order Posted on live chat");
//                        } else {
//                            logger.info("adhoc delivery fail for orderId: {}", id);
//                        }
//                    }
//                }
//            }
//        } else {
//            cart.setCompletionStatus("OnHold");
//            cart.setPaymentStatus("Failed");
//            logger.info("payment fail orderId: {}", id);
//        }
//
//        orderRepository.save(cart);
//        logger.info("cart updated for orderId: {}", id);
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
            Date from, Date to, Example<Order> example) {

        return (Specification<Order>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("created"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("created"), to));
            }
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

}
