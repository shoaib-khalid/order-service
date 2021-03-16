package com.kalsym.order.service.controller;

import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderPaymentDetail;
import com.kalsym.order.service.model.repository.OrderPaymentDetailRepository;
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
@RequestMapping("/orders/{orderId}/payment-details")
public class OrderPaymentDetailController {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderPaymentDetailRepository orderPaymentDetailRepository;

    @PostMapping(path = {""}, name = "order-payment-details-get-by-order")
    @PreAuthorize("hasAnyAuthority('order-payment-details-get-by-order', 'all')")
    public ResponseEntity<HttpResponse> getOrderPaymentDetailsByOrder(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-payment-details-get-by-order, orderId: {}", orderId);

        Optional<Order> order = orderRepository.findById(orderId);

        if (!order.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            logger.info("order-payment-details-get-by-order, orderId, not found. orderId: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderPaymentDetailRepository.findByOrderId(orderId, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "order-payment-details-post-by-order")
    @PreAuthorize("hasAnyAuthority('order-payment-details-post-by-order', 'all')")
    public ResponseEntity<HttpResponse> postOrderPaymentDetailsByOrder(HttpServletRequest request,
            @Valid @RequestBody OrderPaymentDetail bodyOrderPaymentDetail) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-payment-details-post-by-order, orderId: {}", bodyOrderPaymentDetail.getOrderId());
        logger.info(bodyOrderPaymentDetail.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(bodyOrderPaymentDetail.getOrderId());
        if (savedOrder == null) {
            logger.info("order-payment-details-post-by-order, orderId not found, orderId: {}", bodyOrderPaymentDetail.getOrderId());
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        OrderPaymentDetail orderPaymentDetail;
        try {
            orderPaymentDetail = orderPaymentDetailRepository.save(bodyOrderPaymentDetail);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            logger.error("Error saving orderPaymentDetail", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("orderPaymentDetail created with id: " + orderPaymentDetail.getOrderId());
        response.setData(orderPaymentDetail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {""}, name = "order-payment-details-delete-by-order")
    @PreAuthorize("hasAnyAuthority('order-payment-details-delete-by-order', 'all')")
    public ResponseEntity<HttpResponse> deleteOrderPaymentDetailsByOrder(HttpServletRequest request,
            @Valid @RequestBody OrderPaymentDetail bodyOrderPaymentDetail) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-payment-details-delete-by-order, orderId: {}", bodyOrderPaymentDetail.getOrderId());
        logger.info(bodyOrderPaymentDetail.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(bodyOrderPaymentDetail.getOrderId());
        if (savedOrder == null) {
            logger.info("order-payment-details-delete-by-order, order not found, orderId: {}", bodyOrderPaymentDetail.getOrderId());
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }

        try {
            orderPaymentDetailRepository.delete(bodyOrderPaymentDetail);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            logger.error("Error deleting orderPaymentDetail", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("orderPaymentDetail deleted with orderId: " + bodyOrderPaymentDetail.getOrderId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(path = {""}, name = "order-payment-details-put-by-order")
    @PreAuthorize("hasAnyAuthority('order-payment-details-put-by-order', 'all')")
    public ResponseEntity<HttpResponse> putOrderPaymentDetailsByOrder(HttpServletRequest request,
            @Valid @RequestBody OrderPaymentDetail bodyOrderPaymentDetail) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-payment-details-put-by-order, orderId: {}", bodyOrderPaymentDetail.getOrderId());
        logger.info(bodyOrderPaymentDetail.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(bodyOrderPaymentDetail.getOrderId());

        if (!optOrder.isPresent()) {
            logger.info("Order not found with orderId: {}", bodyOrderPaymentDetail.getOrderId());
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Optional<OrderPaymentDetail> optOrderPaymentDetail = orderPaymentDetailRepository.findById(bodyOrderPaymentDetail.getOrderId());

        if (!optOrderPaymentDetail.isPresent()) {
            logger.info("orderPaymentDetail not found with orderId: {}", bodyOrderPaymentDetail.getOrderId());
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("orderPaymentDetail found with orderId: {}", bodyOrderPaymentDetail.getOrderId());
        OrderPaymentDetail orderPaymentDetail = optOrderPaymentDetail.get();

        orderPaymentDetail.update(bodyOrderPaymentDetail);

        logger.info("orderPaymentDetail updated for orderId: {}", bodyOrderPaymentDetail.getOrderId());
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderPaymentDetailRepository.save(orderPaymentDetail));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
