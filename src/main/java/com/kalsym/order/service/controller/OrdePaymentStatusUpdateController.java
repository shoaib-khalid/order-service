package com.kalsym.order.service.controller;

import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderPaymentStatusUpdate;
import com.kalsym.order.service.model.repository.OrderPaymentStatusUpdateRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.utility.HttpResponse;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/orders/{orderId}/payment-status-updates")
public class OrdePaymentStatusUpdateController {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderPaymentStatusUpdateRepository orderPaymentStatusUpdateRepository;

    @GetMapping(path = {""}, name = "order-payment-status-update-get")
    @PreAuthorize("hasAnyAuthority('order-payment-status-update-get', 'all')")
    public ResponseEntity<HttpResponse> getOrderPaymentStatusUpdates(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-payment-status-update-get, orderId: {}", orderId);

        Optional<Order> order = orderRepository.findById(orderId);

        if (!order.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            logger.info("order-payment-status-update-get, orderId, not found. orderId: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderPaymentStatusUpdateRepository.findByOrderId(orderId, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "order-payment-status-update-post")
    @PreAuthorize("hasAnyAuthority('order-payment-status-update-post', 'all')")
    public ResponseEntity<HttpResponse> postOrderPaymentStatusUpdates(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @Valid @RequestBody OrderPaymentStatusUpdate bodyOrderPaymentStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-payment-status-update-post, orderId: {}", bodyOrderPaymentStatusUpdate.getOrderId());
        logger.info(bodyOrderPaymentStatusUpdate.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(bodyOrderPaymentStatusUpdate.getOrderId());
        if (savedOrder == null) {
            logger.info("order-payment-status-update-post-by-order, orderId not found, orderId: {}", bodyOrderPaymentStatusUpdate.getOrderId());
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        OrderPaymentStatusUpdate orderPaymentStatusUpdate;
        try {
            orderPaymentStatusUpdate = orderPaymentStatusUpdateRepository.save(bodyOrderPaymentStatusUpdate);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            logger.error("Error saving orderPaymentStatusUpdate", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("orderPaymentStatusUpdate created with id: " + orderPaymentStatusUpdate.getOrderId());
        response.setData(orderPaymentStatusUpdate);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {"/{id}"}, name = "order-payment-status-update-delete-by-id")
    @PreAuthorize("hasAnyAuthority('order-payment-status-update-delete-by-id', 'all')")
    public ResponseEntity<HttpResponse> deleteOrderPaymentStatusUpdatesById(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @PathVariable(required = true) String id,
            @Valid @RequestBody OrderPaymentStatusUpdate bodyOrderPaymentStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-payment-status-update-delete-by-id, orderId: {}", orderId);
        logger.info(bodyOrderPaymentStatusUpdate.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(orderId);
        if (savedOrder == null) {
            logger.info("order-payment-status-update-delete-by-id, order not found, orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }

        try {
            orderPaymentStatusUpdateRepository.delete(bodyOrderPaymentStatusUpdate);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            logger.error("Error deleting orderPaymentStatusUpdate", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("orderPaymentStatusUpdate deleted with orderId: " + bodyOrderPaymentStatusUpdate.getOrderId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(path = {"/{id}"}, name = "order-payment-status-update-put-by-id")
    @PreAuthorize("hasAnyAuthority('order-payment-status-update-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> putOrderPaymentStatusUpdatesById(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @PathVariable(required = true) String id,
            @Valid @RequestBody OrderPaymentStatusUpdate bodyOrderPaymentStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-payment-status-update-put-by-id, orderId: {}", orderId);
        logger.info(bodyOrderPaymentStatusUpdate.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            logger.info("Order not found with orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Optional<OrderPaymentStatusUpdate> optOrderPaymentStatusUpdate = orderPaymentStatusUpdateRepository.findById(orderId);

        if (!optOrderPaymentStatusUpdate.isPresent()) {
            logger.info("orderPaymentStatusUpdate not found with orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("orderPaymentStatusUpdate found with orderId: {}", orderId);
        OrderPaymentStatusUpdate orderPaymentStatusUpdate = optOrderPaymentStatusUpdate.get();

        orderPaymentStatusUpdate.update(bodyOrderPaymentStatusUpdate);

        logger.info("orderPaymentStatusUpdate updated for orderId: {}", orderId);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderPaymentStatusUpdateRepository.save(orderPaymentStatusUpdate));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
