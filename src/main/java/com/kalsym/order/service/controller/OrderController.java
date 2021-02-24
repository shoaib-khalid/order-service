package com.kalsym.order.service.controller;

import com.kalsym.order.service.model.repository.CartRepository;
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
import com.kalsym.order.service.model.Cart;

/**
 *
 * @author 7cu
 */
@RestController()
@RequestMapping("/orders")
public class OrderController {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    CartRepository cartRepository;

    @GetMapping(path = {""}, name = "order-get", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('order-get', 'all')")
    //@RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json", params = {"storeId", "name", "featured"})
    public ResponseEntity<HttpResponse> getOrder(HttpServletRequest request,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false, defaultValue = "true") boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Cart cartMatch = new Cart();
        cartMatch.setCustomerId(customerId);
        
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
}
