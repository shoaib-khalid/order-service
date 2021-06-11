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
@RequestMapping("/orders/{orderId}/completion-status-updates")
public class OrderCompletionStatusUpdateController {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderCompletionStatusUpdateRepository orderCompletionStatusUpdateRepository;

    @GetMapping(path = {""}, name = "order-completion-status-update-get")
    @PreAuthorize("hasAnyAuthority('order-completion-status-update-get', 'all')")
    public ResponseEntity<HttpResponse> getOrderCompletionStatusUpdates(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-completion-status-update-get, orderId: {}", orderId);

        Optional<Order> order = orderRepository.findById(orderId);

        if (!order.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            logger.info("order-completion-status-update-get, orderId, not found. orderId: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderCompletionStatusUpdateRepository.findByOrderId(orderId, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "order-completion-status-update-post")
    @PreAuthorize("hasAnyAuthority('order-completion-status-update-post', 'all')")
    public ResponseEntity<HttpResponse> postOrderCompletionStatusUpdates(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @Valid @RequestBody OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-completion-status-update-post, orderId: {}", orderId);
        logger.info(bodyOrderCompletionStatusUpdate.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(bodyOrderCompletionStatusUpdate.getOrderId());
        if (savedOrder == null) {
            logger.info("order-completion-status-update-post, orderId not found, orderId: {}", orderId);
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
        logger.info("orderCompletionStatusUpdate created for orderId: {}", orderId);
        response.setData(orderCompletionStatusUpdate);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {"/{id}"}, name = "order-completion-status-update-delete-by-id")
    @PreAuthorize("hasAnyAuthority('order-completion-status-update-delete-by-id', 'all')")
    public ResponseEntity<HttpResponse> deleteOrderCompletionStatusUpdatesById(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @PathVariable(required = true) String id,
            @Valid @RequestBody OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-completion-status-update-delete-by-id, orderId: {}", orderId);
        logger.info(bodyOrderCompletionStatusUpdate.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(orderId);
        if (savedOrder == null) {
            logger.info("order-completion-status-update-delete-by-id, order not found, orderId: {}", orderId);
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

    @PutMapping(path = {"/{id}"}, name = "order-completion-status-update-put-by-id")
    @PreAuthorize("hasAnyAuthority('order-completion-status-update-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> putOrderCompletionStatusUpdatesById(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @PathVariable(required = true) String id,
            @Valid @RequestBody OrderCompletionStatusUpdate bodyOrderCompletionStatusUpdate) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-completion-status-update-put-by-id, orderId: {}, id: {}", orderId, id);
        logger.info(bodyOrderCompletionStatusUpdate.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            logger.info("Order not found with orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Optional<OrderCompletionStatusUpdate> optOrderCompletionStatusUpdate = orderCompletionStatusUpdateRepository.findById(orderId);

        if (!optOrderCompletionStatusUpdate.isPresent()) {
            logger.info("orderCompletionStatusUpdate not found with orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("orderCompletionStatusUpdate found with orderId: {}", orderId);
        OrderCompletionStatusUpdate orderCompletionStatusUpdate = optOrderCompletionStatusUpdate.get();

        orderCompletionStatusUpdate.update(bodyOrderCompletionStatusUpdate);

        logger.info("orderCompletionStatusUpdate updated for orderId: {}", orderId);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderCompletionStatusUpdateRepository.save(orderCompletionStatusUpdate));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
