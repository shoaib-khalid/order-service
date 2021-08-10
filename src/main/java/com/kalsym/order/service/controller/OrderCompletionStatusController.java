package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderCompletionStatus;
import com.kalsym.order.service.model.repository.OrderCompletionStatusRepository;
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
@RequestMapping("/orders/completion-statuses")
public class OrderCompletionStatusController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderCompletionStatusRepository orderCompletionStatusRepository;

    @GetMapping(path = {""}, name = "order-completion-statuses-get")
    @PreAuthorize("hasAnyAuthority('order-completion-statuses-get', 'all')")
    public ResponseEntity<HttpResponse> getOrderCompletionStatuses(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        String logprefix = request.getRequestURI() + " ";

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderCompletionStatusRepository.findAll(pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "order-completion-statuses-post")
    @PreAuthorize("hasAnyAuthority('order-completion-statuses-post', 'all')")
    public ResponseEntity<HttpResponse> postOrderCompletionStatuses(HttpServletRequest request,
            @Valid @RequestBody OrderCompletionStatus bodyOrderCompletionStatus) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logprefix = request.getRequestURI() + " ";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "bodyOrderCompletionStatus.toString(): {}", bodyOrderCompletionStatus.toString());

        Optional<Order> savedOrder = null;

        OrderCompletionStatus orderCompletionStatus;
        try {
            orderCompletionStatus = orderCompletionStatusRepository.save(bodyOrderCompletionStatus);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving orderCompletionStatus", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatus row added");
        response.setData(orderCompletionStatus);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {"/{status}"}, name = "order-completion-statuses-delete-by-status")
    @PreAuthorize("hasAnyAuthority('order-completion-statuses-delete-by-status', 'all')")
    public ResponseEntity<HttpResponse> deleteOrderCompletionStatusesByStatus(HttpServletRequest request,
            @PathVariable(required = true) String status,
            @Valid @RequestBody OrderCompletionStatus bodyOrderCompletionStatus) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-completion-statuses-delete-by-status, status: {}", status);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, bodyOrderCompletionStatus.toString(), "");

        try {
            orderCompletionStatusRepository.deleteById(status);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error deleting orderCompletionStatus", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatus deleted with status: {}", bodyOrderCompletionStatus.getStatus());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(path = {"/{status}"}, name = "order-completion-statuses-put-by-status")
    @PreAuthorize("hasAnyAuthority('order-completion-statuses-put-by-status', 'all')")
    public ResponseEntity<HttpResponse> putOrderCompletionStatusesByStatus(HttpServletRequest request,
            @PathVariable(required = true) String status,
            @Valid @RequestBody OrderCompletionStatus bodyOrderCompletionStatus) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-completion-statuses-put-by-status, status: {}", status);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "bodyOrderCompletionStatus.toString(): {}", bodyOrderCompletionStatus.toString());

        Optional<OrderCompletionStatus> optOrderCompletionStatus = orderCompletionStatusRepository.findById(status);

        if (!optOrderCompletionStatus.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatus not found with status: {}", status);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatus found with status: {}", status);
        OrderCompletionStatus orderCompletionStatus = optOrderCompletionStatus.get();

        orderCompletionStatus.update(bodyOrderCompletionStatus);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderCompletionStatus updated for status: {}", status);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderCompletionStatusRepository.save(orderCompletionStatus));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
