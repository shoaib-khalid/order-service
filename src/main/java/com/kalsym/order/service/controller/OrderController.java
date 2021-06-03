package com.kalsym.order.service.controller;

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
import org.springframework.data.domain.ExampleMatcher;
import com.kalsym.order.service.utility.OrderPostService;
import com.kalsym.order.service.utility.DeliveryService;
import com.kalsym.order.service.utility.EmailService;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.Product;
import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.Store;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.object.DeliveryServiceDeliveryDetails;
import com.kalsym.order.service.model.object.DeliveryServiceSubmitOrder;
import com.kalsym.order.service.model.object.DeliveryServicePickupDetails;
import com.kalsym.order.service.model.object.OrderObject;
import com.kalsym.order.service.model.object.DeliveryServiceResponse;
import com.kalsym.order.service.model.repository.CartRepository;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.model.repository.ProductRepository;
import com.kalsym.order.service.model.repository.StoreRepository;
import com.kalsym.order.service.model.repository.OrderShipmentDetailRepository;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import java.util.Optional;
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

    @GetMapping(path = {""}, name = "orders-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get', 'all')")
    public ResponseEntity<HttpResponse> getOrders(HttpServletRequest request,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        logger.info("orders-get request");
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Order orderMatch = new Order();
        orderMatch.setStoreId(storeId);
        orderMatch.setCustomerId(customerId);

        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<Order> orderExample = Example.of(orderMatch, matcher);

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderRepository.findAll(orderExample, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(path = {"/{id}"}, name = "orders-get-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get-by-id', 'all')")
    public ResponseEntity<HttpResponse> getOrdersById(HttpServletRequest request,
            @PathVariable(required = true) String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Order orderMatch = new Order();
        orderMatch.setId(id);

        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<Order> orderExample = Example.of(orderMatch, matcher);

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderRepository.findAll(orderExample, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "orders-post")
    @PreAuthorize("hasAnyAuthority('orders-post', 'all')")
    public ResponseEntity<HttpResponse> postOrders(HttpServletRequest request,
            @Valid @RequestBody OrderObject bodyOrder) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("orders-post", "");
        logger.info(bodyOrder.toString(), "");

        Order savedOrder = new Order();
        try {

            //create customerId            
            savedOrder.setCartId(bodyOrder.getCartId());
            savedOrder.setCustomerId(bodyOrder.getCustomerId());
            savedOrder.setStoreId(bodyOrder.getStoreId());
            savedOrder.setPaymentStatus(bodyOrder.getPaymentStatus());
            savedOrder.setTotal(bodyOrder.getTotal());
            savedOrder.setSubTotal(bodyOrder.getSubTotal());
            savedOrder.setCustomerNotes(bodyOrder.getCustomerNotes());
            savedOrder.setPrivateAdminNotes(bodyOrder.getPrivateAdminNotes());
            orderRepository.save(savedOrder);
            logger.info("Order created with id: {}", savedOrder.getId());
            //save order item
            List<CartItem> cartItems = cartItemRepository.findByCartId(bodyOrder.getCartId());
            for (int i = 0; i < cartItems.size(); i++) {
                CartItem cartItem = cartItems.get(i);
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(savedOrder.getId());
                orderItem.setItemCode(cartItem.getItemCode());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setWeight(cartItem.getWeight());
                orderItem.setPrice(cartItem.getPrice());
                orderItem.setProductPrice(cartItem.getProductPrice());
                orderItem.setSKU(cartItem.getSKU());
                orderItemRepository.save(orderItem);
            }
            logger.info("Order Item copied for orderId: {}", savedOrder.getId());
            OrderShipmentDetail orderShipmentDetail = new OrderShipmentDetail();
            orderShipmentDetail.setOrderId(savedOrder.getId());
            orderShipmentDetail.setReceiverName(bodyOrder.getDeliveryContactName());
            orderShipmentDetail.setAddress(bodyOrder.getDeliveryAddress());
            orderShipmentDetail.setCity(bodyOrder.getDeliveryCity());
            orderShipmentDetail.setZipcode(bodyOrder.getDeliveryPostcode());
            orderShipmentDetail.setPhoneNumber(bodyOrder.getDeliveryContactPhone());
            orderShipmentDetail.setEmail(bodyOrder.getDeliveryEmail());
            orderShipmentDetail.setDeliveryProviderId(bodyOrder.getDeliveryProviderId());
            orderShipmentDetailRepository.save(orderShipmentDetail);
            logger.info("orderShipmentDetail created for orderId: {}", savedOrder.getId());
            response.setSuccessStatus(HttpStatus.CREATED);
            //clear cart item
            logger.info("clear cartItem for cartId: {}", bodyOrder.getCartId());
            cartItemRepository.clearCartItem(bodyOrder.getCartId());
            // pass orderId to OrderPostService, even though the status is not completed yet
            orderPostService.postOrderLink(savedOrder.getId(), bodyOrder.getStoreId());
        } catch (Exception exp) {
            logger.error("Error saving order", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        
        //Optional<Order> orderDetails = orderRepository.findById(savedOrder.getId());
        response.setData(savedOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
     * @param bodyProduct
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

    /**
     *
     * @param request
     * @param id
     * @param bodyOrder
     * @param bodyProduct
     * @return
     */
    @PostMapping(path = {"/update/{id}"}, name = "orders-update-by-id")
    @PreAuthorize("hasAnyAuthority('orders-update-by-id', 'all')")
    public ResponseEntity<HttpResponse> updateOrdersById(HttpServletRequest request,
            @PathVariable String id,
            @RequestBody OrderObject bodyOrder) {
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

        if (bodyOrder.getPaymentStatus().equalsIgnoreCase("SUCCESS")) {
            order.setCompletionStatus("Received");
            order.setPaymentStatus("Completed");
            logger.info("order success for orderId: {}", id);
            //check if need adhoc delivery        
            List<OrderItem> itemList = orderItemRepository.findByOrderId(order.getId());
            logger.info("orderId:{} itemList size:{}", order.getId(), itemList.size());
            if (itemList.size() > 0) {
                Optional<Product> product = productRepository.findById(itemList.get(0).getProductId());
                if (product.isPresent()) {
                    logger.info("orderId:{} Product found:{} deliveryType:{}", order.getId(), product.get().getId(), product.get().getDeliveryType());
                    if (product.get().getDeliveryType().equals("ADHOC")) {
                        // trigger delivery service
                        DeliveryServiceSubmitOrder deliveryServiceSubmitOrder = new DeliveryServiceSubmitOrder();
                        OrderShipmentDetail orderShipmentDetail = orderShipmentDetailRepository.findByOrderId(id);
                        deliveryServiceSubmitOrder.setOrderId(id);
                        deliveryServiceSubmitOrder.setCustomerId(order.getCustomerId());
                        deliveryServiceSubmitOrder.setStoreId(order.getStoreId());
                        deliveryServiceSubmitOrder.setPieces(1);
                        deliveryServiceSubmitOrder.setProductCode("document");
                        deliveryServiceSubmitOrder.setItemType("parcel");
                        deliveryServiceSubmitOrder.setTotalWeightKg(1);
                        deliveryServiceSubmitOrder.setIsInsurance(false);
                        deliveryServiceSubmitOrder.setShipmentContent("Food");
                        deliveryServiceSubmitOrder.setShipmentValue(order.getTotal());
                        deliveryServiceSubmitOrder.setDeliveryProviderId(orderShipmentDetail.getDeliveryProviderId());
                        //pickup details
                        logger.info("Find storeId:{}", order.getStoreId());
                        Optional<Store> fstore = storeRepository.findById(order.getStoreId());
                        if (fstore.isPresent()) {
                            Store store = fstore.get();
                            logger.info("storeId:{} Store found. contactName:{}", order.getStoreId(), store.getContactName());
                            DeliveryServicePickupDetails pickupDetails = new DeliveryServicePickupDetails();
                            pickupDetails.setPickupContactName(store.getContactName());
                            pickupDetails.setTrolleyRequired(false);
                            pickupDetails.setPickupContactPhone(store.getPhone());
                            pickupDetails.setPickupContactEmail(store.getEmail());
                            pickupDetails.setPickupAddress(store.getAddress());
                            pickupDetails.setPickupPostcode(store.getPostcode());
                            pickupDetails.setPickupCity(store.getCity());
                            pickupDetails.setPickupState(store.getState());
                            pickupDetails.setPickupOption("ADHOC");
                            pickupDetails.setVehicleType("MOTORCYCLE");
                            deliveryServiceSubmitOrder.setPickup(pickupDetails);
                        }
                        //delivery details
                        DeliveryServiceDeliveryDetails deliveryDetails = new DeliveryServiceDeliveryDetails();
                        deliveryDetails.setDeliveryAddress(orderShipmentDetail.getAddress());
                        deliveryDetails.setDeliveryCity(orderShipmentDetail.getCity());
                        deliveryDetails.setDeliveryPostcode(orderShipmentDetail.getZipcode());
                        deliveryDetails.setDeliveryState(orderShipmentDetail.getState());
                        deliveryDetails.setDeliveryCountry(orderShipmentDetail.getCountry());
                        deliveryDetails.setDeliveryContactName(orderShipmentDetail.getReceiverName());
                        deliveryDetails.setDeliveryContactPhone(orderShipmentDetail.getPhoneNumber());
                        deliveryDetails.setDeliveryContactEmail(orderShipmentDetail.getEmail());
                        deliveryServiceSubmitOrder.setDelivery(deliveryDetails);
                        logger.info("submit to delivey-service orderId:{}", order.getId());
                        logger.info("Request Body:{}", deliveryServiceSubmitOrder.toString());
                        DeliveryServiceResponse deliveryResponse = deliveryService.submitDeliveryOrder(deliveryServiceSubmitOrder);
                        logger.info("Response Data isSuccess:{} trackingUrl:{}", deliveryResponse.data.isSuccess, deliveryResponse.data.trackingUrl);
                        if (deliveryResponse.data.isSuccess) {
                            order.setCompletionStatus("ReadyForDelivery");
                            //send email with tracking url
                            String[] url = deliveryResponse.data.trackingUrl;
                            String receiver = orderShipmentDetail.getEmail();
                            String subject = "[" + order.getId() + "] Your order is being deliver";
                            String content = "Your order " + order.getId() + " is being deliver. Use this url to track your order :"
                                    + "<br/>";
                            for (int i = 0; i < url.length; i++) {
                                content += "<br/>" + url[0];
                            }
                            logger.debug("Sending Email! Receiver:" + receiver + " Subject:" + subject + " Content:" + content);
                            emailService.SendEmail(receiver, subject, content);
                            logger.info("Sent Email");
                            // pass orderId to OrderPostService
                            logger.debug("Posting order");
                            orderPostService.postOrderLink(id, bodyOrder.getStoreId());
                            logger.info("Order Posted on live chat");
                        } else {
                            logger.info("adhoc delivery fail for orderId: {}", id);
                        }
                    }
                }
            }
        } else {
            order.setCompletionStatus("OnHold");
            order.setPaymentStatus("Failed");
            logger.info("payment fail orderId: {}", id);
        }

        orderRepository.save(order);
        logger.info("order updated for orderId: {}", id);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(order);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

}
