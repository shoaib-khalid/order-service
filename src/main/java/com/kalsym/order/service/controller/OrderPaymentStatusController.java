package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.OrderPaymentStatus;
import com.kalsym.order.service.model.repository.OrderPaymentStatusRepository;
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
@RequestMapping("/orders/payment-statuseses")
public class OrderPaymentStatusController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderPaymentStatusRepository orderPaymentStatusRepository;

    @GetMapping(path = {""}, name = "order-payment-statuses-get")
    @PreAuthorize("hasAnyAuthority('order-payment-statuses-get', 'all')")
    public ResponseEntity<HttpResponse> getOrderPaymentStatuses(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderPaymentStatusRepository.findAll(pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "order-payment-statuses-post")
    @PreAuthorize("hasAnyAuthority('order-payment-statuses-post', 'all')")
    public ResponseEntity<HttpResponse> postOrderPaymentStatuses(HttpServletRequest request,
            @Valid @RequestBody OrderPaymentStatus bodyOrderPaymentStatus) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        OrderPaymentStatus orderPaymentStatusUpdate;
        try {
            orderPaymentStatusUpdate = orderPaymentStatusRepository.save(bodyOrderPaymentStatus);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving orderPaymentStatus", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatus created with status: " + orderPaymentStatusUpdate.getStatus());
        response.setData(orderPaymentStatusUpdate);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {"/{status}"}, name = "order-payment-statuses-delete-by-id")
    @PreAuthorize("hasAnyAuthority('order-payment-statuses-delete-by-id', 'all')")
    public ResponseEntity<HttpResponse> deleteOrderPaymentStatusesByStatus(HttpServletRequest request,
            @PathVariable(required = true) String status,
            @Valid @RequestBody OrderPaymentStatus bodyOrderPaymentStatus) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "bodyOrderPaymentStatus.toString(): {}", bodyOrderPaymentStatus.toString());

        try {
            orderPaymentStatusRepository.delete(bodyOrderPaymentStatus);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error deleting orderPaymentStatus", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatusUpdate deleted with orderId: " + bodyOrderPaymentStatus.getStatus());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(path = {"/{status}"}, name = "order-payment-statuses-put-by-id")
    @PreAuthorize("hasAnyAuthority('order-payment-statuses-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> putOrderPaymentStatusesByStatus(HttpServletRequest request,
            @PathVariable(required = true) String status,
            @Valid @RequestBody OrderPaymentStatus bodyOrderPaymentStatus) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-payment-statuses-put-by-id, status: {}", status);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "bodyOrderPaymentStatus.toString(): {}", bodyOrderPaymentStatus.toString());

        Optional<OrderPaymentStatus> optOrderPaymentStatus = orderPaymentStatusRepository.findById(status);

        if (!optOrderPaymentStatus.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatusUpdate not found with orderId: {}", status);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatus found with status: {}", status);
        OrderPaymentStatus orderPaymentStatus = optOrderPaymentStatus.get();

        orderPaymentStatus.update(bodyOrderPaymentStatus);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderPaymentStatusUpdate updated for status: {}", status);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderPaymentStatusRepository.save(orderPaymentStatus));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
