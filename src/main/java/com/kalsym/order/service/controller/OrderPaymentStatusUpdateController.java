package com.kalsym.order.service.controller;

import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.repository.OrderPaymentStatusUpdateRepository;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
import com.kalsym.order.service.model.OrderPaymentStatusUpdate;
import com.kalsym.order.service.model.repository.OrderCompletionStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.service.EmailService;
import com.kalsym.order.service.service.OrderPostService;
import com.kalsym.order.service.utility.HttpResponse;
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
    OrderPaymentStatusUpdateRepository orderPaymentStatusUpdateRepository;

    @Autowired
    OrderCompletionStatusUpdateRepository orderCompletionStatusUpdateRepository;

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
    @PutMapping(path = {""}, name = "order-completion-status-update")
    @PreAuthorize("hasAnyAuthority('order-completion -status-update', 'all')")
    public ResponseEntity<HttpResponse> putOrderCompletionStatusUpdatesConfirm(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @Valid @RequestBody OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-payment-status-update-put, orderId: {}", orderId);
        logger.info(bodyOrderCompletionStatusUpdate.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            logger.info("Order not found with orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND, "order not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Order order = optOrder.get();
//        String status = bodyOrderCompletionStatusUpdate.getStatus();
        OrderStatus status = bodyOrderCompletionStatusUpdate.getStatus();
        String subject = null;
        String content = null;
        //String[] url = deliveryResponse.data.trackingUrl;
        String receiver = order.getOrderShipmentDetail().getEmail();
        OrderPaymentStatusUpdate orderPaymentStatusUpdate = new OrderPaymentStatusUpdate();
        switch (status) {
            case AWAITING_PICKUP:
                subject = "[" + order.getId() + "] Your order is awaiting pickup";
                content = "Your order " + order.getId() + " is awaiting pickup. Use this url to track your order :"
                        + "<br/>";
                break;
            case BEING_DELIVERED:
                subject = "[" + order.getId() + "] Your order is being delivered";
                content = "Your order " + order.getId() + " is being delivered. Use this url to track your order :"
                        + "<br/>";
                break;
            case BEING_PREPARED:
                subject = "[" + order.getId() + "] Your order is being prepared";
                content = "Your order " + order.getId() + " is being prepared. Use this url to track your order :"
                        + "<br/>";
                break;
            case CANCELED_BY_CUSTOMER:
                subject = "[" + order.getId() + "] Order Canceled";
                content = "Your order " + order.getId() + " is has been canceled. ";
                order.setCompletionStatus(OrderStatus.CANCELED_BY_CUSTOMER);
                break;
            case DELIVERED_TO_CUSTOMER:
                subject = "[" + order.getId() + "] Your order is delivered";
                content = "Your order " + order.getId() + " is delivered. thank you for using our services";
                order.setCompletionStatus(OrderStatus.DELIVERED_TO_CUSTOMER);
                break;
            case PAYMENT_CONFIRMED:
//                order.setCompletionStatus(OrderStatus.PAYMENT_CONFIRMED);
                subject = "[" + order.getId() + "] Your order amount is paid";
                content = "Your order " + order.getId() + " amount is paid. Use this url to track your order :"
                        + "<br/>";
                emailService.sendEmail(receiver, subject, content);
                boolean isExceptionOccur = false;
                try {
                    deliveryService.confirmOrderDelivery(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId());
                    status = OrderStatus.AWAITING_PICKUP;
                } catch (Exception ex) {
                    //there might be some issue so need to updated email for issue and refund
                    isExceptionOccur = true;
                    status = OrderStatus.REQUESTING_DELIVERY_FAILED;
                }
                orderPaymentStatusUpdate.setStatus(PaymentStatus.PAID);
                orderPaymentStatusUpdate.setComments(bodyOrderCompletionStatusUpdate.getComments());
                orderPaymentStatusUpdate.setModifiedBy(bodyOrderCompletionStatusUpdate.getModifiedBy());
                orderPaymentStatusUpdate.setOrderId(orderId);
                orderPaymentStatusUpdateRepository.save(orderPaymentStatusUpdate);
                logger.info("orderPaymentStatusUpdate created with orderId: {}", orderId);

                order.setPaymentStatus(PaymentStatus.PAID);
                if (!isExceptionOccur) {
                    subject = "[" + order.getId() + "] Your order is awaiting pickup";
                    content = "Your order " + order.getId() + " is awaiting pickup. Use this url to track your order :"
                            + "<br/>";
                } else {
                    subject = "[" + order.getId() + "] Your order is not placed";
                    content = "Your order " + order.getId() + " is not placed successfully due some technical issue. Please try again later. "
                            + "<br/>";
                }
                break;
            case READY_FOR_DELIVERY:
                subject = "[" + order.getId() + "] Your order is Ready for delivery";
                content = "Your order " + order.getId() + " is ready for delivery. Use this url to track your order :"
                        + "<br/>";
                break;
            case RECEIVED_AT_STORE:
                subject = "[" + order.getId() + "] Your order is recieved";
                content = "Your order " + order.getId() + " is recieved. Use this url to track your order :"
                        + "<br/>";
                break;
            case REFUNDED:
                subject = "[" + order.getId() + "] Request for refund";
                content = "Your order " + order.getId() + " refund is requested. Use this url to track your order :"
                        + "<br/>";
                break;
            case REJECTED_BY_STORE:
                subject = "[" + order.getId() + "] Your order is rejected by store";
                content = "Sorry! \nYour order " + order.getId() + " is rejected by store owner. Use this url to track your order :"
                        + "<br/>";
                break;
            case REQUESTING_DELIVERY_FAILED:
                subject = "[" + order.getId() + "] Your order Delivery is failed";
                content = "Your order " + order.getId() + " is not accepted by the delivery firm . Use this url to track your order :"
                        + "<br/>";
                break;
            case FAILED:
                subject = "[" + order.getId() + "] Your order is Failed";
                content = "Sorry! \nYour order is not placed. Please try again"
                        + "<br/>";
                order.setPaymentStatus(PaymentStatus.FAILED);
                orderPaymentStatusUpdate.setStatus(PaymentStatus.FAILED);
                orderPaymentStatusUpdate.setComments(bodyOrderCompletionStatusUpdate.getComments());
                orderPaymentStatusUpdate.setModifiedBy(bodyOrderCompletionStatusUpdate.getModifiedBy());
                orderPaymentStatusUpdate.setOrderId(orderId);
                orderPaymentStatusUpdateRepository.save(orderPaymentStatusUpdate);
                logger.info("orderPaymentStatusUpdate created with orderId: {}", orderId);
                break;
        }
        order.setCompletionStatus(status);
//        orderRepository.save(order);
//        Optional<OrderPaymentStatusUpdate> optOrderPaymentStatusUpdate = orderPaymentStatusUpdateRepository.findById(orderId);
//
//        if (!optOrderPaymentStatusUpdate.isPresent()) {
//            logger.info("orderPaymentStatusUpdate not found with orderId: {}", orderId);
//            response.setErrorStatus(HttpStatus.NOT_FOUND);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }

        orderCompletionStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);

        orderRepository.save(order);
        logger.info("Order completion status updated to : " + status.toString());
//        for (int i = 0; i < url.length; i++) {
//            content += "<br/>" + url[0];
//        }
        emailService.sendEmail(receiver, subject, content);
        try {
            orderPostService.postOrderLink(order.getId(), order.getStoreId());
        } catch (Exception e) {
            logger.info("error sending message to rocket chat: {}", e);
        }

        logger.info("orderPaymentStatusUpdate updated for orderId: {}", orderId);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(order);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

}
