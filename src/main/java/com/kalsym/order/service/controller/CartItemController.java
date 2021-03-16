package com.kalsym.order.service.controller;

import com.kalsym.order.service.model.Cart;
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.CartRepository;
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
@RequestMapping("/carts/{cartId}/items")
public class CartItemController {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @PostMapping(path = {""}, name = "cart-items-get-by-cart")
    @PreAuthorize("hasAnyAuthority('cart-items-get-by-cart', 'all')")
    public ResponseEntity<HttpResponse> getCartItemsByCart(HttpServletRequest request,
            @PathVariable(required = true) String cartId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("cart-items-get-by-cart, cartId: {}", cartId);

        Optional<Cart> cart = cartRepository.findById(cartId);

        if (!cart.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            logger.info("cart-items-get-by-cart, cartId, not found. cartId: {}", cartId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(cartItemRepository.findByCartId(cartId, pageable));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {"/{cartItemId}"}, name = "cart-items-get-by-cartItemId")
    @PreAuthorize("hasAnyAuthority('cart-items-get-by-cartItemId', 'all')")
    public ResponseEntity<HttpResponse> getCartItemsByCartItemId(HttpServletRequest request,
            @PathVariable(required = true) String cartItemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("cart-items-get-by-cartItemId, cartItemId: {}", cartItemId);

        Optional<CartItem> cartItem = cartItemRepository.findById(cartItemId);

        if (!cartItem.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            logger.info("cart-items-get-by-cartItemId, cartItemId, not found. cartItemId: {}", cartItemId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pageable pageable = PageRequest.of(page, pageSize);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(cartItemRepository.findById(cartItemId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = {""}, name = "cart-items-post-by-cart")
    @PreAuthorize("hasAnyAuthority('cart-items-post-by-cart', 'all')")
    public ResponseEntity<HttpResponse> postCartItemsByCart(HttpServletRequest request,
            @Valid @RequestBody CartItem bodyCartItem) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("cart-items-post-by-cart, cartId: {}", bodyCartItem.getCartId());
        logger.info(bodyCartItem.toString(), "");

        Optional<Cart> savedCart = null;

        savedCart = cartRepository.findById(bodyCartItem.getCartId());
        if (savedCart == null) {
            logger.info("cart-items-post-by-cart, cartId not found, cartId: {}", bodyCartItem.getCartId());
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }
        CartItem cartItem;
        try {
            cartItem = cartItemRepository.save(bodyCartItem);
            response.setSuccessStatus(HttpStatus.CREATED);
        } catch (Exception exp) {
            logger.error("Error saving cart item", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("cartItem created with id: " + cartItem.getId());
        response.setData(cartItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(path = {""}, name = "cart-items-delete-by-cart")
    @PreAuthorize("hasAnyAuthority('cart-items-delete-by-cart', 'all')")
    public ResponseEntity<HttpResponse> deleteCartItemsByCart(HttpServletRequest request,
            @Valid @RequestBody CartItem bodyCartItem) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("cart-items-delete-by-cart, cartId: {}", bodyCartItem.getCartId());
        logger.info(bodyCartItem.toString(), "");

        Optional<Cart> savedCart = null;

        savedCart = cartRepository.findById(bodyCartItem.getCartId());
        if (savedCart == null) {
            logger.info("cart-items-delete-by-cart, cartId not found, cartId: {}", bodyCartItem.getCartId());
            response.setErrorStatus(HttpStatus.FAILED_DEPENDENCY);
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(response);
        }

        try {
            cartItemRepository.delete(bodyCartItem);
            response.setSuccessStatus(HttpStatus.OK);
        } catch (Exception exp) {
            logger.error("Error deleting cart item", exp);
            response.setMessage(exp.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
        logger.info("cartItem deleted with id: " + bodyCartItem.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(path = {""}, name = "cart-items-put-by-cart")
    @PreAuthorize("hasAnyAuthority('cart-items-put-by-cart', 'all')")
    public ResponseEntity<HttpResponse> putCartItemsByCart(HttpServletRequest request,
            @Valid @RequestBody CartItem bodyCartItem) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        logger.info("cart-items-put-by-cart, cartId: {}", bodyCartItem.getCartId());
        logger.info(bodyCartItem.toString(), "");

        Optional<Cart> optCart = cartRepository.findById(bodyCartItem.getCartId());

        if (!optCart.isPresent()) {
            logger.info("Cart not found with cartId: {}", bodyCartItem.getCartId());
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Optional<CartItem> optCartItem = cartItemRepository.findById(bodyCartItem.getId());

        if (!optCartItem.isPresent()) {
            logger.info("CartItem not found with cartItemId: {}", bodyCartItem.getId());
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        logger.info("cartItem found with cartItemId: {}", bodyCartItem.getId());
        CartItem cartItem = optCartItem.get();

        cartItem.update(bodyCartItem);

        logger.info("cartItem updated for cartItemId: {}", bodyCartItem.getId());
        response.setSuccessStatus(HttpStatus.ACCEPTED);
        response.setData(cartItemRepository.save(cartItem));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }


}
