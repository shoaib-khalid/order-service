package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderPaymentDetail;
import com.kalsym.order.service.model.repository.OrderPaymentDetailRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Logger;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
@RequestMapping("/orders/{orderId}/payment-details")
public class OrderPaymentDetailController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderPaymentDetailRepository orderPaymentDetailRepository;

    @GetMapping(path = {""}, name = "order-payment-details-get")
    @PreAuthorize("hasAnyAuthority('order-payment-details-get', 'all')")
    public ResponseEntity<HttpResponse> getOrderPaymentDetails(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {

        String logprefix = request.getRequestURI();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-details-get-by-order, orderId: {}", orderId);

        Optional<Order> order = orderRepository.findById(orderId);

        if (!order.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-details-get-by-order, orderId, not found. orderId: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderPaymentDetailRepository.findByOrderId(orderId, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "order-payment-details-post")
    @PreAuthorize("hasAnyAuthority('order-payment-details-post', 'all')")
    public ResponseEntity<HttpResponse> postOrderPaymentDetails(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @Valid @RequestBody OrderPaymentDetail bodyOrderPaymentDetail) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-details-post, orderId: {}", orderId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderPaymentDetail.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(orderId);
        if (savedOrder == null) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-details-post, orderId not found, orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        OrderPaymentDetail orderPaymentDetail;
        try {
            orderPaymentDetail = orderPaymentDetailRepository.save(bodyOrderPaymentDetail);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving orderPaymentDetail", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentDetail created with id: " + orderId);
        response.setData(orderPaymentDetail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {"/{id}"}, name = "order-payment-details-delete-by-id")
    @PreAuthorize("hasAnyAuthority('order-payment-details-delete-by-id', 'all')")
    public ResponseEntity<HttpResponse> deleteOrderPaymentDetailsById(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @PathVariable(required = true) String id,
            @Valid @RequestBody OrderPaymentDetail bodyOrderPaymentDetail) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-details-delete-by-id, orderId: {}", orderId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderPaymentDetail.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(orderId);
        if (savedOrder == null) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-details-delete-by-id, order not found, orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }

        try {
            orderPaymentDetailRepository.delete(bodyOrderPaymentDetail);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error deleting orderPaymentDetail", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentDetail deleted with orderId: " + orderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(path = {"/{id}"}, name = "order-payment-details-put-by-id")
    @PreAuthorize("hasAnyAuthority('order-payment-details-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> putOrderPaymentDetailsById(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @PathVariable(required = true) String id,
            @Valid @RequestBody OrderPaymentDetail bodyOrderPaymentDetail) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-details-put-by-id, orderId: {}", orderId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderPaymentDetail.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Optional<OrderPaymentDetail> optOrderPaymentDetail = orderPaymentDetailRepository.findById(orderId);

        if (!optOrderPaymentDetail.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentDetail not found with orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentDetail found with orderId: {}", orderId);
        OrderPaymentDetail orderPaymentDetail = optOrderPaymentDetail.get();

        orderPaymentDetail.update(bodyOrderPaymentDetail);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentDetail updated for orderId: {}", orderId);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderPaymentDetailRepository.save(orderPaymentDetail));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
