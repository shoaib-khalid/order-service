package com.kalsym.order.service.controller;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import com.kalsym.order.service.utility.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.repository.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import java.util.Optional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * GET /orders GET /orders/{id} POST /orders DELETE /orders/{id} PUT
 * /orders/{id}
 *
 * @author 7cu
 */
@RestController()
@RequestMapping("/orders")
public class OrderController {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    OrderRepository orderRepository;

    @GetMapping(path = {""}, name = "orders-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get', 'all')")
    public ResponseEntity<HttpResponse> getOrders(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Order orderMatch = new Order();

        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<Order> orderExample = Example.of(orderMatch, matcher);

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderRepository.findAll(orderExample, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(path = {"/{orderId}"}, name = "orders-get-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-get-by-id', 'all')")
    public ResponseEntity<HttpResponse> getOrdersById(HttpServletRequest request,
            @RequestParam(required = true) String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Order orderMatch = new Order();
        orderMatch.setId(orderId);

        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<Order> orderExample = Example.of(orderMatch, matcher);

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderRepository.findAll(orderExample, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "orders-post")
    @PreAuthorize("hasAnyAuthority('orders-post', 'all')")
    public ResponseEntity<HttpResponse> postOrders(HttpServletRequest request,
            @Valid @RequestBody Order bodyOrder) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("orders-post", "");
        logger.info(bodyOrder.toString(), "");

        Order savedStore = null;
        try {
            savedStore = orderRepository.save(bodyOrder);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            logger.error("Error saving order", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("order created with id: " + savedStore.getId());
        response.setData(savedStore);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {"/{id}"}, name = "orders-delete-by-id")
    @PreAuthorize("hasAnyAuthority('orders-delete-by-id', 'all')")
    public ResponseEntity<HttpResponse> deleteOrdersById(HttpServletRequest request,
            @PathVariable(required = true) String id) {
        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("orders-delete-by-id, orderId: {}", id);

        Optional<Order> optProduct = orderRepository.findById(id);

        if (!optProduct.isPresent()) {
            logger.info("order not found", "");
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("order found", "");
        orderRepository.delete(optProduct.get());

        logger.info("order deleted, with orderId: {}", id);
        response.setSuccessStatus(HttpStatus.OK);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     *
     * @param request
     * @param orderId
     * @param bodyProduct
     * @return
     */
    @PutMapping(path = {"/{id}"}, name = "orders-put-by-id")
    @PreAuthorize("hasAnyAuthority('orders-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> putOrdersById(HttpServletRequest request, 
            @PathVariable String id, 
            @RequestBody Order bodyOrder) {
        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("", "");
        logger.info(bodyOrder.toString(), "");

        Optional<Order> optOrder = orderRepository.findById(id);

        if (!optOrder.isPresent()) {
            logger.info("Order not found with orderId: {}", id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("order found with orderId: {}", id);
        Order order = optOrder.get();
        List<String> errors = new ArrayList<>();

        order.update(bodyOrder);

        logger.info("order updated for orderId: {}", id);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderRepository.save(order));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

}
