package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.enums.ProductStatus;
import com.kalsym.order.service.enums.StorePaymentType;
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
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.model.*;
import com.kalsym.order.service.model.repository.OrderShipmentDetailRepository;
import com.kalsym.order.service.utility.Logger;

/**
 *
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
    EmailService emailService;

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

//    @GetMapping(path = {""}, name = "order-payment-status-update-get")
//    @PreAuthorize("hasAnyAuthority('order-payment-status-update-get', 'all')")
//    public ResponseEntity<HttpResponse> getOrderPaymentStatusUpdates(HttpServletRequest request,
//            @PathVariable(required = true) String orderId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
//        HttpResponse response = new HttpResponse(request.getRequestURI());
//
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-status-update-get, orderId: {}", orderId);
//
//        Optional<Order> order = orderRepository.findById(orderId);
//
//        if (!order.isPresent()) {
//            response.setErrorStatus(HttpStatus.NOT_FOUND);
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-status-update-get, orderId, not found. orderId: {}", orderId);
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
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-status-update-post, orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderCompletionStatusUpdate.toString(), "");
//
//        Optional<Order> savedOrder = orderRepository.findById(bodyOrderCompletionStatusUpdate.getOrderId());
//
//        if (!savedOrder.isPresent()) {
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-status-update-post-by-order, orderId not found, orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
//            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
//            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
//        }
//        try {
//            orderPaymentStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);
//            response.setSuccessStatus(HttpStatus.CREATED);
//        } catch (Exception exp) {
//            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving orderPaymentStatusUpdate", exp);
//            response.setMessage(exp.getMessage());
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
//        }
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatusUpdate created with id: " + bodyOrderCompletionStatusUpdate.getOrderId());
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
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-status-update-delete-by-id, orderId: {}", orderId);
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderCompletionStatusUpdate.toString(), "");
//
//        Optional<Order> savedOrder = orderRepository.findById(orderId);
//        if (!savedOrder.isPresent()) {
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-status-update-delete-by-id, order not found, orderId: {}", orderId);
//            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
//            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
//        }
//
//        try {
//            orderPaymentStatusUpdateRepository.delete(bodyOrderCompletionStatusUpdate);
//            response.setSuccessStatus(HttpStatus.OK);
//        } catch (Exception exp) {
//            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error deleting orderPaymentStatusUpdate", exp);
//            response.setMessage(exp.getMessage());
//            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
//        }
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatusUpdate deleted with orderId: " + bodyOrderCompletionStatusUpdate.getOrderId());
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
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-status-update-put, orderId: {}", orderId);
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderCompletionStatusUpdate.toString(), "");
//
//        Optional<Order> optOrder = orderRepository.findById(orderId);
//
//        if (!optOrder.isPresent()) {
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: {}", orderId);
//            response.setErrorStatus(HttpStatus.NOT_FOUND);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//        Optional<OrderPaymentStatusUpdate> optOrderPaymentStatusUpdate = orderPaymentStatusUpdateRepository.findById(orderId);
//
//        if (!optOrderPaymentStatusUpdate.isPresent()) {
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatusUpdate not found with orderId: {}", orderId);
//            response.setErrorStatus(HttpStatus.NOT_FOUND);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatusUpdate found with orderId: {}", orderId);
//        bodyOrderCompletionStatusUpdate = orderPaymentStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);
//
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatusUpdate updated for orderId: {}", orderId);
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

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-completion-status-updates-put-by-order-id, orderId: " + orderId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderCompletionStatusUpdate.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: " + orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND, "order not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        Order order = optOrder.get();
        Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(order.getStoreId());
        if (!optStore.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store not found with storeId: " + order.getStoreId());
            response.setErrorStatus(HttpStatus.NOT_FOUND, "Store not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        StoreWithDetails storeWithDetails = optStore.get();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Store details got : " + storeWithDetails.toString());
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
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "clear cartItem for cartId: " + order.getCartId());
//                order.setCompletionStatus(OrderStatus.PAYMENT_CONFIRMED);
                //inserting order payment status update
                orderPaymentStatusUpdate.setStatus(PaymentStatus.PAID);
                orderPaymentStatusUpdate.setComments(bodyOrderCompletionStatusUpdate.getComments());
                orderPaymentStatusUpdate.setModifiedBy(bodyOrderCompletionStatusUpdate.getModifiedBy());
                orderPaymentStatusUpdate.setOrderId(orderId);
                orderPaymentStatusUpdateRepository.save(orderPaymentStatusUpdate);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatusUpdate created with orderId: " + orderId);
                //inserting order completing status update
//                orderCompletionStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);
//                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatusUpdate updated for orderId: {}, with orderStatus: {}", orderId, status.toString());
                try {
                    ProductInventory productInventory;
                    Product product;
                    //sending request to rocket chat for posting order
                    orderPostService.postOrderLink(order.getId(), order.getStoreId(), orderItems);
                    for (int i = 0; i < orderItems.size(); i++) {
                        // get product details

                        product = productService.getProductById(order.getStoreId(), orderItems.get(i).getProductId());
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Got product details of orderItem: " + product.toString());
                        if (product.isTrackQuantity()) {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Product tracking is enable");
                            productInventory = productService.reduceProductInventory(order.getStoreId(), orderItems.get(i).getProductId(), orderItems.get(i).getItemCode(), orderItems.get(i).getQuantity());
                            if (productInventory.getQuantity() <= product.getMinQuantityForAlarm()) {
                                //sending notification for product is going out of stock
                                //we can send email as well
                                orderPostService.sendMinimumQuantityAlarm(order.getId(), order.getStoreId(), orderItems.get(i), productInventory.getQuantity());
                                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "intimation send for out of stock product id: " + orderItems.get(i).getProductId() + ", SKU: " + orderItems.get(i).getSKU() + ", Name: " + productInventory.getProduct().getName());
                            }

                            if (!product.isAllowOutOfStockPurchases() && productInventory.getQuantity() <= 0) {
                                // making this product variant outof stock
                                productInventory = productService.changeProductStatus(order.getStoreId(), orderItems.get(i).getProductId(), orderItems.get(i).getItemCode(), ProductStatus.OUTOFSTOCK);
                                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "this product variant is out of stock now storeId: " + order.getStoreId() + ", productId: " + orderItems.get(i).getProductId() + ", itemCode: " + orderItems.get(i).getItemCode());
                            }
                        } else {
                            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Product tracking is not enabled by marchant");
                        }
//                        ProductInventory productInventory = productInventoryRepository.findByItemCode(orderItems.get(i).getItemCode());
//                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got product inventory details: " + productInventory.toString());
                        //reduce quantity of product inventory

                    }

                    //check store.verticalCode, if FnB do not request to delivery service
                    // commenting below code reason is: merchant have to request manually to delivery service
//                    if (!"FNB".equalsIgnoreCase(storeWithDetails.getVerticalCode())) {
//                        DeliveryOrder deliveryOrder = deliveryService.confirmOrderDelivery(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId(), order.getId());
////                    status = OrderStatus.AWAITING_PICKUP;
//                        email.getBody().setMerchantTrackingUrl(deliveryOrder.getMerchantTrackingUrl());
//                        email.getBody().setCustomerTrackingUrl(deliveryOrder.getCustomerTrackingUrl());
//                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "delivery confirmed for order: {} awaiting for pickup", orderId);
//                    } else {
//                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "This order is FnB so no need to call delivery confirmation now, storeId: " + storeWithDetails.getId());
//                    }
                    //sending request to rocket chat for posting order
                    //orderPostService.postOrderLink(order.getId(), order.getStoreId(), orderItems);
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order posted to rocket chat");
                } catch (Exception ex) {
                    //there might be some issue so need to updated email for issue and refund
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Exception occur ", ex);
//                    if (!"FNB".equalsIgnoreCase(storeWithDetails.getVerticalCode())) {
//                    status = OrderStatus.REQUESTING_DELIVERY_FAILED;
//                    status = OrderStatus.FAILED;
//                    }

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

            case READY_FOR_DELIVERY:

                //sending email
                //emailService.sendEmail(email);
                break;
        }
        order.setCompletionStatus(status);
//        order.getOrderShipmentDetail().setTrackingUrl(bodyOrderCompletionStatusUpdate.getTrackingUrl());
//        //setting email body status
//        email.getBody().setOrderStatus(status);

        //inserting order completion status updates
        orderCompletionStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatusUpdate updated for orderId: " + orderId + ", with orderStatus: " + status.toString());

        orderRepository.save(order);

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

            DeliveryOrder deliveryOrder = deliveryService.confirmOrderDelivery(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId(), order.getId());
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
