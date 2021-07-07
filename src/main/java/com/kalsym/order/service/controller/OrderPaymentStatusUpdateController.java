package com.kalsym.order.service.controller;

import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.model.Body;
import com.kalsym.order.service.model.Email;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.repository.OrderPaymentStatusUpdateRepository;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderPaymentStatusUpdate;
import com.kalsym.order.service.model.repository.OrderCompletionStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.model.repository.StoreDetailsRepository;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.service.EmailService;
import com.kalsym.order.service.service.OrderPostService;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Utilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kalsym.order.service.model.DeliveryOrder;
import com.kalsym.order.service.model.repository.CartItemRepository;

/**
 *
 * @author 7cu
 */
@RestController()
@RequestMapping("/orders/{orderId}/completion-status-updates")
public class OrderPaymentStatusUpdateController {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    DeliveryService deliveryService;

    @Autowired
    EmailService emailService;

    @Autowired
    OrderPostService orderPostService;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderPaymentStatusUpdateRepository orderPaymentStatusUpdateRepository;

    @Autowired
    OrderCompletionStatusUpdateRepository orderCompletionStatusUpdateRepository;

    @Autowired
    StoreDetailsRepository storeDetailsRepository;

    @Autowired
    CartItemRepository cartItemRepository;

//    @GetMapping(path = {""}, name = "order-payment-status-update-get")
//    @PreAuthorize("hasAnyAuthority('order-payment-status-update-get', 'all')")
//    public ResponseEntity<HttpResponse> getOrderPaymentStatusUpdates(HttpServletRequest request,
//            @PathVariable(required = true) String orderId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
//        HttpResponse response = new HttpResponse(request.getRequestURI());
//
//        logger.info("order-payment-status-update-get, orderId: {}", orderId);
//
//        Optional<Order> order = orderRepository.findById(orderId);
//
//        if (!order.isPresent()) {
//            response.setErrorStatus(HttpStatus.NOT_FOUND);
//            logger.info("order-payment-status-update-get, orderId, not found. orderId: {}", orderId);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//
//        Pageable pageable = PageRequest.of(page, pageSize);
//
//        response.setSuccessStatus(HttpStatus.OK);
//        response.setData(orderPaymentStatusUpdateRepository.findByOrderId(orderId, pageable));
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    @PostMapping(path = {""}, name = "order-payment-status-update-post")
//    @PreAuthorize("hasAnyAuthority('order-payment-status-update-post', 'all')")
//    public ResponseEntity<HttpResponse> postOrderPaymentStatusUpdates(HttpServletRequest request,
//            @PathVariable(required = true) String orderId,
//            @Valid @RequestBody OrderPaymentStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
//        String logprefix = request.getRequestURI() + " ";
//        HttpResponse response = new HttpResponse(request.getRequestURI());
//
//        logger.info("order-payment-status-update-post, orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
//        logger.info(bodyOrderCompletionStatusUpdate.toString(), "");
//
//        Optional<Order> savedOrder = orderRepository.findById(bodyOrderCompletionStatusUpdate.getOrderId());
//
//        if (!savedOrder.isPresent()) {
//            logger.info("order-payment-status-update-post-by-order, orderId not found, orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
//            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
//            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
//        }
//        try {
//            orderPaymentStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);
//            response.setSuccessStatus(HttpStatus.CREATED);
//        } catch (Exception exp) {
//            logger.error("Error saving orderPaymentStatusUpdate", exp);
//            response.setMessage(exp.getMessage());
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
//        }
//        logger.info("orderPaymentStatusUpdate created with id: " + bodyOrderCompletionStatusUpdate.getOrderId());
//        response.setData(bodyOrderCompletionStatusUpdate);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
//    @DeleteMapping(path = {"/{id}"}, name = "order-payment-status-update-delete-by-id")
//    @PreAuthorize("hasAnyAuthority('order-payment-status-update-delete-by-id', 'all')")
//    public ResponseEntity<HttpResponse> deleteOrderPaymentStatusUpdatesById(HttpServletRequest request,
//            @PathVariable(required = true) String orderId,
//            @PathVariable(required = true) String id,
//            @Valid @RequestBody OrderPaymentStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
//        String logprefix = request.getRequestURI() + " ";
//        HttpResponse response = new HttpResponse(request.getRequestURI());
//
//        logger.info("order-payment-status-update-delete-by-id, orderId: {}", orderId);
//        logger.info(bodyOrderCompletionStatusUpdate.toString(), "");
//
//        Optional<Order> savedOrder = orderRepository.findById(orderId);
//        if (!savedOrder.isPresent()) {
//            logger.info("order-payment-status-update-delete-by-id, order not found, orderId: {}", orderId);
//            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
//            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
//        }
//
//        try {
//            orderPaymentStatusUpdateRepository.delete(bodyOrderCompletionStatusUpdate);
//            response.setSuccessStatus(HttpStatus.OK);
//        } catch (Exception exp) {
//            logger.error("Error deleting orderPaymentStatusUpdate", exp);
//            response.setMessage(exp.getMessage());
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
//        }
//        logger.info("orderPaymentStatusUpdate deleted with orderId: " + bodyOrderCompletionStatusUpdate.getOrderId());
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//    @PutMapping(path = {"/"}, name = "order-payment-status-update-put")
//    @PreAuthorize("hasAnyAuthority('order-payment-status-update-put', 'all')")
//    public ResponseEntity<HttpResponse> putOrderPaymentStatusUpdatesById(HttpServletRequest request,
//            @PathVariable(required = true) String orderId,
//            @PathVariable(required = true) String paymentId,
//            @Valid @RequestBody OrderPaymentStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
//        String logprefix = request.getRequestURI() + " ";
//        HttpResponse response = new HttpResponse(request.getRequestURI());
//
//        logger.info("order-payment-status-update-put, orderId: {}", orderId);
//        logger.info(bodyOrderCompletionStatusUpdate.toString(), "");
//
//        Optional<Order> optOrder = orderRepository.findById(orderId);
//
//        if (!optOrder.isPresent()) {
//            logger.info("Order not found with orderId: {}", orderId);
//            response.setErrorStatus(HttpStatus.NOT_FOUND);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//        Optional<OrderPaymentStatusUpdate> optOrderPaymentStatusUpdate = orderPaymentStatusUpdateRepository.findById(orderId);
//
//        if (!optOrderPaymentStatusUpdate.isPresent()) {
//            logger.info("orderPaymentStatusUpdate not found with orderId: {}", orderId);
//            response.setErrorStatus(HttpStatus.NOT_FOUND);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//        logger.info("orderPaymentStatusUpdate found with orderId: {}", orderId);
//        bodyOrderCompletionStatusUpdate = orderPaymentStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);
//
//        logger.info("orderPaymentStatusUpdate updated for orderId: {}", orderId);
//        response.setSuccessStatus(HttpStatus.ACCEPTED);
//        response.setData(orderPaymentStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate));
//        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
//    }
    @PutMapping(path = {""}, name = "order-completion-status-updates-put-by-order-id")
    @PreAuthorize("hasAnyAuthority('order-completion-status-updates-put-by-order-id', 'all')")
    public ResponseEntity<HttpResponse> putOrderCompletionStatusUpdatesConfirm(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @Valid @RequestBody OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-completion-status-updates-put-by-order-id, orderId: {}", orderId);
        logger.info(bodyOrderCompletionStatusUpdate.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            logger.info("Order not found with orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND, "order not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Order order = optOrder.get();
        Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(order.getStoreId());
        if (!optStore.isPresent()) {
            logger.info("Store not found with storeId: {}", order.getStoreId());
            response.setErrorStatus(HttpStatus.NOT_FOUND, "Store not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        StoreWithDetails storeWithDetails = optStore.get();
        logger.info("Store details got : " + storeWithDetails.toString());
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
        switch (status) {
            case PAYMENT_CONFIRMED:
                //clear cart item
                cartItemRepository.clearCartItem(order.getCartId());
                logger.info("clear cartItem for cartId: {}", order.getCartId());
//                order.setCompletionStatus(OrderStatus.PAYMENT_CONFIRMED);
                //inserting order payment status update
                orderPaymentStatusUpdate.setStatus(PaymentStatus.PAID);
                orderPaymentStatusUpdate.setComments(bodyOrderCompletionStatusUpdate.getComments());
                orderPaymentStatusUpdate.setModifiedBy(bodyOrderCompletionStatusUpdate.getModifiedBy());
                orderPaymentStatusUpdate.setOrderId(orderId);
                orderPaymentStatusUpdateRepository.save(orderPaymentStatusUpdate);
                logger.info("orderPaymentStatusUpdate created with orderId: {}", orderId);
                //inserting order completing status update
                orderCompletionStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);
                logger.info("orderCompletionStatusUpdate updated for orderId: {}, with orderStatus: {}", orderId, status.toString());
                try {
                    //TODO: check store.verticalCode, if FnB do not request to delivery service
                    if (!"FNB".equalsIgnoreCase(storeWithDetails.getVerticalCode())) {
                        DeliveryOrder deliveryOrder = deliveryService.confirmOrderDelivery(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId(), order.getId());
//                    status = OrderStatus.AWAITING_PICKUP;
                        email.getBody().setMerchantTrackingUrl(deliveryOrder.getMerchantTrackingUrl());
                        email.getBody().setCustomerTrackingUrl(deliveryOrder.getCustomerTrackingUrl());
                        logger.info("delivery confirmed for order: {} awaiting for pickup", orderId);
                    } else {
                        logger.info("This order is FnB so no need to call delivery confirmation now, storeId: " + storeWithDetails.getId());
                    }
                    //sending request to rocket chat for posting order
                    orderPostService.postOrderLink(order.getId(), order.getStoreId(), orderItems);
                } catch (Exception ex) {
                    //there might be some issue so need to updated email for issue and refund
                    logger.error("Exception occur ", ex);
                    if (!"FNB".equalsIgnoreCase(storeWithDetails.getVerticalCode())) {
                        status = OrderStatus.REQUESTING_DELIVERY_FAILED;
                    }

                }
                //sending email
                emailService.sendEmail(email);
                //setting completing status with update values 
                bodyOrderCompletionStatusUpdate.setStatus(status);
                //setting payment status in order object
                order.setPaymentStatus(PaymentStatus.PAID);

                break;
            case DELIVERED_TO_CUSTOMER:
            case REJECTED_BY_STORE:
                //sending email
                emailService.sendEmail(email);
                break;
        }
        order.setCompletionStatus(status);
//        order.getOrderShipmentDetail().setTrackingUrl(bodyOrderCompletionStatusUpdate.getTrackingUrl());
//        //setting email body status
//        email.getBody().setOrderStatus(status);

        //inserting order completion status updates
        orderCompletionStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);
        logger.info("orderCompletionStatusUpdate updated for orderId: {}, with orderStatus: {}", orderId, status.toString());

        orderRepository.save(order);

//        emailService.sendEmail(email);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(order);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

    }

    @PutMapping(path = {"/request-delivery"}, name = "order-completion-status-updates-put-by-order-id-by-merchant")
    @PreAuthorize("hasAnyAuthority('order-completion-status-updates-put-by-order-id-by-merchant', 'all')")
    public ResponseEntity<HttpResponse> requestToDelivery(HttpServletRequest request,
            @PathVariable(required = true) String orderId) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());
        OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate = new OrderCompletionStatusUpdate();
        logger.info("order-completion-status-updates-put-by-order-id-by-merchant, orderId: {}", orderId);
//        logger.info(bodyOrderCompletionStatusUpdate.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            logger.info("Order not found with orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND, "order not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Order order = optOrder.get();
        Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(order.getStoreId());
        if (!optStore.isPresent()) {
            logger.info("Store not found with storeId: {}", order.getStoreId());
            response.setErrorStatus(HttpStatus.NOT_FOUND, "Store not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        StoreWithDetails storeWithDetails = optStore.get();
        logger.info("Store details got : " + storeWithDetails.toString());
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
        logger.info("orderCompletionStatusUpdate updated for orderId: {}, with orderStatus: {}", orderId, status.toString());
        try {

            DeliveryOrder deliveryOrder = deliveryService.confirmOrderDelivery(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId(), order.getId());
            email.getBody().setMerchantTrackingUrl(deliveryOrder.getMerchantTrackingUrl());
            email.getBody().setCustomerTrackingUrl(deliveryOrder.getCustomerTrackingUrl());
            logger.info("delivery confirmed for order: {} awaiting for pickup", orderId);

        } catch (Exception ex) {
            //there might be some issue so need to updated email for issue and refund
            logger.error("Exception occur while confirming order Delivery ", ex);
            status = OrderStatus.REQUESTING_DELIVERY_FAILED;
        }
        //sending email
        emailService.sendEmail(email);
        //update completion status in order
        order.setCompletionStatus(status);

//        //inserting order completion status updates
//        orderCompletionStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);
//        logger.info("orderPaymentStatusUpdate updated for orderId: {}, with orderStatus: {}", orderId, status.toString());

        orderRepository.save(order);


        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(order);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

    }

}
