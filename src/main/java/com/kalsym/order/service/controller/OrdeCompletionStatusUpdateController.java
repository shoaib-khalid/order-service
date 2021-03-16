package com.kalsym.order.service.controller;

import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
import com.kalsym.order.service.model.repository.OrderCompletionStatusUpdateRepository;
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
@RequestMapping("/orders/{orderId}/completion-status-updates")
public class OrdeCompletionStatusUpdateController {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderCompletionStatusUpdateRepository orderCompletionStatusUpdateRepository;

    @PostMapping(path = {""}, name = "order-completion-status-update-get-by-order")
    @PreAuthorize("hasAnyAuthority('order-completion-status-update-get-by-order', 'all')")
    public ResponseEntity<HttpResponse> getOrderCompletionStatusUpdatesByOrder(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-completion-status-update-get-by-order, orderId: {}", orderId);

        Optional<Order> order = orderRepository.findById(orderId);

        if (!order.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            logger.info("order-completion-status-update-get-by-order, orderId, not found. orderId: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderCompletionStatusUpdateRepository.findByOrderId(orderId, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "order-completion-status-update-post-by-order")
    @PreAuthorize("hasAnyAuthority('order-completion-status-update-post-by-order', 'all')")
    public ResponseEntity<HttpResponse> postOrderCompletionStatusUpdatesByOrder(HttpServletRequest request,
            @Valid @RequestBody OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-completion-status-update-post-by-order, orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
        logger.info(bodyOrderCompletionStatusUpdate.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(bodyOrderCompletionStatusUpdate.getOrderId());
        if (savedOrder == null) {
            logger.info("order-completion-status-update-post-by-order, orderId not found, orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        OrderCompletionStatusUpdate orderCompletionStatusUpdate;
        try {
            orderCompletionStatusUpdate = orderCompletionStatusUpdateRepository.save(bodyOrderCompletionStatusUpdate);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            logger.error("Error saving orderCompletionStatusUpdate", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("orderCompletionStatusUpdate created with id: " + orderCompletionStatusUpdate.getOrderId());
        response.setData(orderCompletionStatusUpdate);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {""}, name = "order-completion-status-update-delete-by-order")
    @PreAuthorize("hasAnyAuthority('order-completion-status-update-delete-by-order', 'all')")
    public ResponseEntity<HttpResponse> deleteOrderCompletionStatusUpdatesByOrder(HttpServletRequest request,
            @Valid @RequestBody OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-completion-status-update-delete-by-order, orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
        logger.info(bodyOrderCompletionStatusUpdate.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(bodyOrderCompletionStatusUpdate.getOrderId());
        if (savedOrder == null) {
            logger.info("order-completion-status-update-delete-by-order, order not found, orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }

        try {
            orderCompletionStatusUpdateRepository.delete(bodyOrderCompletionStatusUpdate);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            logger.error("Error deleting orderCompletionStatusUpdate", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("orderCompletionStatusUpdate deleted with orderId: " + bodyOrderCompletionStatusUpdate.getOrderId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(path = {""}, name = "order-completion-status-update-put-by-order")
    @PreAuthorize("hasAnyAuthority('order-completion-status-update-put-by-order', 'all')")
    public ResponseEntity<HttpResponse> putOrderCompletionStatusUpdatesByOrder(HttpServletRequest request,
            @Valid @RequestBody OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-completion-status-update-put-by-order, orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
        logger.info(bodyOrderCompletionStatusUpdate.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(bodyOrderCompletionStatusUpdate.getOrderId());

        if (!optOrder.isPresent()) {
            logger.info("Order not found with orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Optional<OrderCompletionStatusUpdate> optOrderCompletionStatusUpdate = orderCompletionStatusUpdateRepository.findById(bodyOrderCompletionStatusUpdate.getOrderId());

        if (!optOrderCompletionStatusUpdate.isPresent()) {
            logger.info("orderCompletionStatusUpdate not found with orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("orderCompletionStatusUpdate found with orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
        OrderCompletionStatusUpdate orderCompletionStatusUpdate = optOrderCompletionStatusUpdate.get();

        orderCompletionStatusUpdate.update(bodyOrderCompletionStatusUpdate);

        logger.info("orderCompletionStatusUpdate updated for orderId: {}", bodyOrderCompletionStatusUpdate.getOrderId());
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderCompletionStatusUpdateRepository.save(orderCompletionStatusUpdate));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
