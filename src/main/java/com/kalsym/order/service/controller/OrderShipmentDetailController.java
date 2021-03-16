package com.kalsym.order.service.controller;

import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.repository.OrderShipmentDetailRepository;
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
@RequestMapping("/orders/{orderId}/shipment-details")
public class OrderShipmentDetailController {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderShipmentDetailRepository orderShipmentDetailRepository;

    @PostMapping(path = {""}, name = "order-shipment-details-get-by-order")
    @PreAuthorize("hasAnyAuthority('order-shipment-details-get-by-order', 'all')")
    public ResponseEntity<HttpResponse> getOrderShipmentDetailsByOrder(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-shipment-details-get-by-order, orderId: {}", orderId);

        Optional<Order> order = orderRepository.findById(orderId);

        if (!order.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            logger.info("order-shipment-details-get-by-order, orderId, not found. orderId: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderShipmentDetailRepository.findByOrderId(orderId, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "order-shipment-details-post-by-order")
    @PreAuthorize("hasAnyAuthority('order-shipment-details-post-by-order', 'all')")
    public ResponseEntity<HttpResponse> postOrderShipmentDetailsByOrder(HttpServletRequest request,
            @Valid @RequestBody OrderShipmentDetail bodyOrderShipmentDetail) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-shipment-details-post-by-order, orderId: {}", bodyOrderShipmentDetail.getOrderId());
        logger.info(bodyOrderShipmentDetail.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(bodyOrderShipmentDetail.getOrderId());
        if (savedOrder == null) {
            logger.info("order-shipment-details-post-by-order, orderId not found, orderId: {}", bodyOrderShipmentDetail.getOrderId());
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        OrderShipmentDetail orderShipmentDetail;
        try {
            orderShipmentDetail = orderShipmentDetailRepository.save(bodyOrderShipmentDetail);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            logger.error("Error saving orderShipmentDetail", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("orderShipmentDetail created with id: " + orderShipmentDetail.getOrderId());
        response.setData(orderShipmentDetail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {""}, name = "order-shipment-details-delete-by-order")
    @PreAuthorize("hasAnyAuthority('order-shipment-details-delete-by-order', 'all')")
    public ResponseEntity<HttpResponse> deleteOrderShipmentDetailsByOrder(HttpServletRequest request,
            @Valid @RequestBody OrderShipmentDetail bodyOrderShipmentDetail) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-shipment-details-delete-by-order, orderId: {}", bodyOrderShipmentDetail.getOrderId());
        logger.info(bodyOrderShipmentDetail.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(bodyOrderShipmentDetail.getOrderId());
        if (savedOrder == null) {
            logger.info("order-shipment-details-delete-by-order, order not found, orderId: {}", bodyOrderShipmentDetail.getOrderId());
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }

        try {
            orderShipmentDetailRepository.delete(bodyOrderShipmentDetail);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            logger.error("Error deleting orderShipmentDetail", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("orderShipmentDetail deleted with orderId: " + bodyOrderShipmentDetail.getOrderId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(path = {""}, name = "order-shipment-details-put-by-order")
    @PreAuthorize("hasAnyAuthority('order-shipment-details-put-by-order', 'all')")
    public ResponseEntity<HttpResponse> putOrderShipmentDetailsByOrder(HttpServletRequest request,
            @Valid @RequestBody OrderShipmentDetail bodyOrderShipmentDetail) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-shipment-details-put-by-order, orderId: {}", bodyOrderShipmentDetail.getOrderId());
        logger.info(bodyOrderShipmentDetail.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(bodyOrderShipmentDetail.getOrderId());

        if (!optOrder.isPresent()) {
            logger.info("Order not found with orderId: {}", bodyOrderShipmentDetail.getOrderId());
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Optional<OrderShipmentDetail> optOrderShipmentDetail = orderShipmentDetailRepository.findById(bodyOrderShipmentDetail.getOrderId());

        if (!optOrderShipmentDetail.isPresent()) {
            logger.info("orderShipmentDetail not found with orderId: {}", bodyOrderShipmentDetail.getOrderId());
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("orderShipmentDetail found with orderId: {}", bodyOrderShipmentDetail.getOrderId());
        OrderShipmentDetail orderItem = optOrderShipmentDetail.get();

        orderItem.update(bodyOrderShipmentDetail);

        logger.info("orderShipmentDetail updated for orderId: {}", bodyOrderShipmentDetail.getOrderId());
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderShipmentDetailRepository.save(orderItem));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
