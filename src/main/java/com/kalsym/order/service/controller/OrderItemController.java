package com.kalsym.order.service.controller;

import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.ProductInventory;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.model.repository.ProductInventoryRepository;
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
@RequestMapping("/orders/{orderId}/items")
public class OrderItemController {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    ProductInventoryRepository productInventoryRepository;

    @GetMapping(path = {""}, name = "order-items-get")
    @PreAuthorize("hasAnyAuthority('order-items-get', 'all')")
    public ResponseEntity<HttpResponse> getOrderItems(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-items-get-by-order, orderId: {}", orderId);

        Optional<Order> order = orderRepository.findById(orderId);

        if (!order.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            logger.info("order-items-get-by-order, orderId, not found. orderId: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderItemRepository.findByOrderId(orderId, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "order-items-post")
    @PreAuthorize("hasAnyAuthority('order-items-post', 'all')")
    public ResponseEntity<HttpResponse> postOrderItems(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @Valid @RequestBody OrderItem bodyOrderItem) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-items-post, url : {}, orderId: {}", request.getRequestURI(), orderId);

        logger.info("bodyOrderItem: {}", bodyOrderItem.toString());

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(orderId);
        if (savedOrder == null) {
            logger.info("order-items-post, cartId not found, orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        OrderItem orderItem;
        /*try {
            //find item in current cart, increase quantity if already exist
            OrderItem existingItem = orderItemRepository.findByOrderIdAndProductId(bodyOrderItem.getOrderId(), bodyOrderItem.getProductId());
            if (existingItem!=null) {
                logger.info("item already exist for orderId: {} with productId: {}", bodyOrderItem.getOrderId(), bodyOrderItem.getProductId());
                int newQty = existingItem.getQuantity() + bodyOrderItem.getQuantity();
                existingItem.setQuantity(newQty);
                orderItem = orderItemRepository.save(existingItem);
            } else {
                orderItem = orderItemRepository.save(bodyOrderItem);
            }
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            logger.error("Error saving orderItem", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }*/

        //find product invertory against itemcode to set sku
        ProductInventory productInventory = productInventoryRepository.findByItemCode(bodyOrderItem.getItemCode());
        logger.info("got product inventory details: " + productInventory.toString());
        bodyOrderItem.setPrice(bodyOrderItem.getProductPrice() * bodyOrderItem.getQuantity());
        bodyOrderItem.setSKU(productInventory.getSKU());
//        bodyOrderItem.setProductName(productInventory.getProduct().getName());
        orderItem = orderItemRepository.save(bodyOrderItem);
        response.setSuccessStatus(HttpStatus.CREATED);
        logger.info("cartItem added in orderId: {} with orderItemId: {}", orderId, orderItem.getId());
        response.setData(orderItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {"/{id}"}, name = "order-items-delete-by-id")
    @PreAuthorize("hasAnyAuthority('order-items-delete-by-id', 'all')")
    public ResponseEntity<HttpResponse> deleteOrderItemsById(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @PathVariable(required = true) String id) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-items-delete-by-id, orderId: {}, orderItemId: {}", orderId, id);

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(orderId);
        if (savedOrder == null) {
            logger.info("order-items-delete-by-order, orderId not found, orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }

        try {
            orderItemRepository.deleteById(id);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            logger.error("Error deleting order item with id: {}", id, exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("orderItem deleted with orderItemId: {}", id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(path = {"/{id}"}, name = "order-items-put-by-id")
    @PreAuthorize("hasAnyAuthority('order-items-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> putCartItemsById(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @PathVariable(required = true) String id,
            @Valid @RequestBody OrderItem bodyOrderItem) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("order-items-put-by-order, orderId: {}, id: {}", orderId, id);
        logger.info("bodyOrderItem.toString()", bodyOrderItem.toString());

        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            logger.info("Order not found with orderId: {}", orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Optional<OrderItem> optOrderItem = orderItemRepository.findById(id);

        if (!optOrderItem.isPresent()) {
            logger.info("orderItem not found with orderItemId: {}", id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("orderItem found with orderItemId: {}", id);
        OrderItem orderItem = optOrderItem.get();

        orderItem.update(bodyOrderItem);

        logger.info("orderItem updated for orderItemId: {}", id);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderItemRepository.save(orderItem));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
