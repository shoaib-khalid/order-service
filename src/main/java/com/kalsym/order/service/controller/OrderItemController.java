package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.ProductInventory;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.model.repository.ProductInventoryRepository;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Logger;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    ProductInventoryRepository productInventoryRepository;

    @Value("${order.item.price.update:false}")
    Boolean orderItemPriceUpdate;

    @GetMapping(path = {""}, name = "order-items-get")
    @PreAuthorize("hasAnyAuthority('order-items-get', 'all')")
    public ResponseEntity<HttpResponse> getOrderItems(HttpServletRequest request,
            @PathVariable(required = true) String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        String logprefix = request.getRequestURI() + " ";

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-items-get-by-order, orderId: " + orderId);

        Optional<Order> order = orderRepository.findById(orderId);

        if (!order.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-items-get-by-order, orderId, not found. orderId: " + orderId);
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
        String logprefix = request.getRequestURI() + " ";

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-items-post, url : " + request.getRequestURI() + ", orderId: " + orderId);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "bodyOrderItem: " + bodyOrderItem.toString());

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(orderId);
        if (savedOrder == null) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-items-post, cartId not found, orderId: " + orderId);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        OrderItem orderItem;
        /*try {
            //find item in current cart, increase quantity if already exist
            OrderItem existingItem = orderItemRepository.findByOrderIdAndProductId(bodyOrderItem.getOrderId(), bodyOrderItem.getProductId());
            if (existingItem!=null) {
                int newQty = existingItem.getQuantity() + bodyOrderItem.getQuantity();
                existingItem.setQuantity(newQty);
                orderItem = orderItemRepository.save(existingItem);
            } else {
                orderItem = orderItemRepository.save(bodyOrderItem);
            }
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error saving orderItem", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }*/

        //find product invertory against itemcode to set sku
        ProductInventory productInventory = productInventoryRepository.findByItemCode(bodyOrderItem.getItemCode());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got product inventory details: " + productInventory.toString());

        if (orderItemPriceUpdate) {
            Double productInventoryPrice = productInventory.getPrice();
            Float productInventoryPriceF = productInventoryPrice.floatValue();
            bodyOrderItem.setProductPrice(productInventoryPriceF);
        }

        bodyOrderItem.setPrice(bodyOrderItem.getProductPrice() * bodyOrderItem.getQuantity());
        bodyOrderItem.setSKU(productInventory.getSKU());
//        bodyOrderItem.setProductName(productInventory.getProduct().getName());
        orderItem = orderItemRepository.save(bodyOrderItem);
        response.setSuccessStatus(HttpStatus.CREATED);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "cartItem added in orderId: " + orderId + " with orderItemId: " + orderItem.getId());
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

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-items-delete-by-id, orderId: " + orderId + ", orderItemId: " + id);

        Optional<Order> savedOrder = null;

        savedOrder = orderRepository.findById(orderId);
        if (savedOrder == null) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-items-delete-by-order, orderId not found, orderId: " + orderId);
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }

        try {
            orderItemRepository.deleteById(id);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error deleting order item with id: " + id, exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderItem deleted with orderItemId: " + id);
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

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order-items-put-by-order, orderId: " + orderId + ", id: " + id);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "bodyOrderItem.toString()", bodyOrderItem.toString());

        Optional<Order> optOrder = orderRepository.findById(orderId);

        if (!optOrder.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found with orderId: " + orderId);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Optional<OrderItem> optOrderItem = orderItemRepository.findById(id);

        if (!optOrderItem.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderItem not found with orderItemId: " + id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderItem found with orderItemId: " + id);
        OrderItem orderItem = optOrderItem.get();

        orderItem.update(bodyOrderItem);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderItem updated for orderItemId: " + id);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(orderItemRepository.save(orderItem));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
