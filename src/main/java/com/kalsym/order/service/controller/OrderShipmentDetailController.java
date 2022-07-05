package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.repository.OrderShipmentDetailRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Logger;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/orders/{orderId}/shipment-details")
public class OrderShipmentDetailController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderShipmentDetailRepository orderShipmentDetailRepository;

    @GetMapping(path = {""}, name = "order-shipment-details-get")
    @PreAuthorize("hasAnyAuthority('order-shipment-details-get', 'all')")
    public ResponseEntity<HttpResponse> getOrderShipmentDetailsByOrder(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        String logprefix = request.getRequestURI() + " ";

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-shipment-details-get, orderId: {}", orderId);

        Optional<Order> order = orderRepository.findById(orderId);

        if (!order.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-shipment-details-get, orderId, not found. orderId: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderShipmentDetailRepository.findByOrderId(orderId, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "order-shipment-details-post")
    @PreAuthorize("hasAnyAuthority('order-shipment-details-post', 'all')")
    public ResponseEntity<HttpResponse> postOrderShipmentDetails(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @Valid @RequestBody OrderShipmentDetail bodyOrderShipmentDetail) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-shipment-details-post, orderId: {}", orderId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderShipmentDetail.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(orderId);
        if (savedOrder == null) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-shipment-details-post, orderId not found, orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        OrderShipmentDetail orderShipmentDetail;
        try {
            orderShipmentDetail = orderShipmentDetailRepository.save(bodyOrderShipmentDetail);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving orderShipmentDetail", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderShipmentDetail created with id: " + orderId);
        response.setData(orderShipmentDetail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {"{id}"}, name = "order-shipment-details-delete-by-id")
    @PreAuthorize("hasAnyAuthority('order-shipment-details-delete-by-id', 'all')")
    public ResponseEntity<HttpResponse> deleteOrderShipmentDetailsById(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @PathVariable(required = true) String id,
            @Valid @RequestBody OrderShipmentDetail bodyOrderShipmentDetail) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-shipment-details-delete-by-id, orderId: {}", orderId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderShipmentDetail.toString(), "");

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(orderId);
        if (savedOrder == null) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-shipment-details-delete-by-id, order not found, orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }

        try {
            orderShipmentDetailRepository.delete(bodyOrderShipmentDetail);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error deleting orderShipmentDetail", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderShipmentDetail deleted with orderId: " + orderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(path = {"/{id}"}, name = "order-shipment-details-put-by-id")
    @PreAuthorize("hasAnyAuthority('order-shipment-details-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> putOrderShipmentDetailsById(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @PathVariable(required = true) String id,
            @Valid @RequestBody OrderShipmentDetail bodyOrderShipmentDetail) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-shipment-details-put-by-id, orderId: {}", orderId);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderShipmentDetail.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

//        Optional<OrderShipmentDetail> optOrderShipmentDetail = orderShipmentDetailRepository.findById(orderId);
//
//        if (!optOrderShipmentDetail.isPresent()) {
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderShipmentDetail not found with orderId: {}", orderId);
//            response.setErrorStatus(HttpStatus.NOT_FOUND);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderShipmentDetail found with orderId: {}", orderId);
//        OrderShipmentDetail orderItem = optOrderShipmentDetail.get();
        bodyOrderShipmentDetail.update(bodyOrderShipmentDetail);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderShipmentDetail updated for orderId: {}", orderId);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderShipmentDetailRepository.save(bodyOrderShipmentDetail));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
        
    
}
