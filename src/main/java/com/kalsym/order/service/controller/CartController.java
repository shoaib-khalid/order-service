package com.kalsym.order.service.controller;

import com.kalsym.order.service.model.repository.CartRepository;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.utility.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.OrderItem;
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

import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.repository.OrderRepository;

/**
 *
 * @author 7cu
 */
@RestController()
@RequestMapping("/carts")
public class CartController {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired

    OrderItemRepository orderItemRepository;
    @Autowired
    OrderRepository orderRepository;

    @GetMapping(path = {""}, name = "carts-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-get', 'all')")
    public ResponseEntity<HttpResponse> getCarts(HttpServletRequest request,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        logger.info("carts-get request...");
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Cart cartMatch = new Cart();
        cartMatch.setCustomerId(customerId);
        cartMatch.setStoreId(storeId);

        ExampleMatcher exampleCartMatcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<Cart> cartExample = Example.of(cartMatch, exampleCartMatcher);

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(cartRepository.findAll(cartExample, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(path = {"/{id}"}, name = "carts-get-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-get-by-id', 'all')")
    public ResponseEntity<HttpResponse> getCartsById(HttpServletRequest request,
            @RequestParam(required = true) String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Cart cartMatch = new Cart();
        cartMatch.setId(id);

        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<Cart> cartExample = Example.of(cartMatch, matcher);

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(cartRepository.findAll(cartExample, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "carts-post")
    @PreAuthorize("hasAnyAuthority('carts-post', 'all')")
    public ResponseEntity<HttpResponse> postCarts(HttpServletRequest request,
            @Valid @RequestBody Cart bodyCart) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("carts-post, bodyCart: ", bodyCart.toString());

        Cart savedCart = null;
        try {
            savedCart = cartRepository.save(bodyCart);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            logger.error("Error saving cart", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("cart created with id: " + savedCart.getId());
        response.setData(savedCart);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {"/{id}"}, name = "carts-delete-by-id")
    @PreAuthorize("hasAnyAuthority('carts-delete-by-id', 'all')")
    public ResponseEntity<HttpResponse> deleteCartsById(HttpServletRequest request, @PathVariable String id) {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("carts-delete-by-id, cartId: {}", id);

        Optional<Cart> optCart = cartRepository.findById(id);

        if (!optCart.isPresent()) {
            logger.info("cart not found with cartId: {}", id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("cart found", "");
        cartRepository.deleteById(id);

        logger.info("cart deleted, with id: {}", id);
        response.setSuccessStatus(HttpStatus.OK);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     *
     * @param request
     * @param id
     * @param bodyCart
     * @return
     */
    @PutMapping(path = {"/{id}"}, name = "carts-put-by-id")
    @PreAuthorize("hasAnyAuthority('carts-put-by-id', 'all')")
    public ResponseEntity<HttpResponse> putCartsById(HttpServletRequest request, @PathVariable String id,
            @Valid @RequestBody Cart bodyCart) {
        String logprefix = request.getRequestURI() + " ";
        String location = Thread.currentThread().getStackTrace()[1].getMethodName();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("", "");
        logger.info(bodyCart.toString(), "");

        Optional<Cart> optCart = cartRepository.findById(id);

        if (!optCart.isPresent()) {
            logger.info("Cart not found with cartId: {}", id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("cart found with cartId: {}", id);
        Cart cart = optCart.get();
        List<String> errors = new ArrayList<>();

        cart.update(bodyCart);

        logger.info("cart updated for cartId: {}", id);
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(cartRepository.save(cart));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * TODO: create endpoint to empty the cart by deleting all the cart-items in the cart
     * @param request
     * @param id
     * @return 
     */
    @GetMapping(path = {"/{id}/empty"}, name = "carts-empty-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('carts-order-by-id', 'all')")
    public ResponseEntity<HttpResponse> empty(HttpServletRequest request,
            @PathVariable String id) {
        //

//        logger.info("carts-order-by-id request...");
//        HttpResponse response = new HttpResponse(request.getRequestURI());
//
//        Cart cartMatch = new Cart();
//
//        Optional<Cart> cartOptional = cartRepository.findById(id);
//
//        Cart cart = cartOptional.get();
//
//        Order newOrder = new Order();
//        newOrder.setCompletionStatus("OnHold");
//        newOrder.setCustomerId(cart.getCustomerId());
//        newOrder.setPaymentStatus("Pending");
//        Order savedOrder = orderRepository.save(newOrder);
//        // Closing the cart status since now order is being placed 
//        cart.setIsOpen(Boolean.FALSE);
//        Iterable<CartItem> cartItems = cartItemRepository.findByCartId(id, null);
//
//        for (CartItem cartItem : cartItems) {
//            OrderItem orderItem = new OrderItem();
//            orderItem.setItemCode(cartItem.getItemCode());
//            orderItem.setOrderId(savedOrder.getId());
//            // Price is missing in cart
//            orderItem.setPrice(cartItem.getId());
//            orderItem.setProductId(cartItem.getProductId());
//            // ProductPrice is also missing in cart
//            orderItem.setProductPrice(cartItem.getProductId());
//            orderItem.setQuantity(cartItem.getQuantity());
//            // SKU is missing in cart
//            orderItem.setSKU(cartItem.getQuantity());
//            orderItem.setWeight(cartItem.getQuantity());
//        }
//
//        ExampleMatcher exampleCartMatcher = ExampleMatcher
//                .matchingAll()
//                .withIgnoreCase()
//                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
//        Example<Cart> cartExample = Example.of(cartMatch, exampleCartMatcher);
//
//        Pageable pageable = PageRequest.of(page, pageSize);
//
//        response.setSuccessStatus(HttpStatus.OK);
//        response.setData(cartRepository.findAll(cartExample, pageable));
//        return ResponseEntity.status(HttpStatus.OK).body(response);
        return null;
    }

}
