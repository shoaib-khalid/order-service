package com.kalsym.order.service.controller;

import com.google.common.collect.Lists;
import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.enums.ProductStatus;
import com.kalsym.order.service.enums.RefundStatus;
import com.kalsym.order.service.enums.RefundType;
import com.kalsym.order.service.enums.StorePaymentType;
import com.kalsym.order.service.model.Body;
import com.kalsym.order.service.model.Email;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.repository.*;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderPaymentStatusUpdate;
import com.kalsym.order.service.service.DeliveryService;
import com.kalsym.order.service.service.EmailService;
import com.kalsym.order.service.service.OrderPostService;
import com.kalsym.order.service.service.WhatsappService;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.MessageGenerator;
import com.kalsym.order.service.utility.Utilities;
import com.kalsym.order.service.utility.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kalsym.order.service.model.DeliveryOrder;
import com.kalsym.order.service.service.ProductService;
import com.kalsym.order.service.model.*;
import com.kalsym.order.service.model.object.OrderProcessResult;
import com.kalsym.order.service.service.FCMService;
import com.kalsym.order.service.utility.Logger;
import java.util.Date;
import javax.persistence.criteria.Predicate;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 7cu
 */
@RestController()
@RequestMapping("/ordergroups")
public class OrderGroupController {

    @Autowired
    OrderRepository orderRepository;
    
    @Autowired
    OrderGroupRepository orderGroupRepository;
    
    @GetMapping(path = {"/{orderGroupId}"}, name = "orders-group-get-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-group-get-by-id', 'all')")
    public ResponseEntity<HttpResponse> getOrderGroupsById(HttpServletRequest request,
            @PathVariable(required = true) String orderGroupId) {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        //remove prefix G
        if (orderGroupId.startsWith("G")) {
            orderGroupId = orderGroupId.substring(1);
        }
        
        Optional<OrderGroup> optOrderGroup = orderGroupRepository.findById(orderGroupId);
        if (!optOrderGroup.isPresent()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("Order group with id " + orderGroupId + " not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        OrderGroup orderGroup = optOrderGroup.get();
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderGroup);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    

     @GetMapping(path = {""}, name = "orders-group-get-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-group-get-by-id', 'all')")
    public ResponseEntity<HttpResponse> getOrderGroups(HttpServletRequest request,
            @RequestParam(required = false) String orderGroupId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        String logprefix = "getOrderGroups()";
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "ordersGroup-get request " + request.getRequestURL());
        HttpResponse response = new HttpResponse(request.getRequestURI());

        OrderGroup orderMatch = new OrderGroup();
        if (orderGroupId != null && !orderGroupId.isEmpty()) {
            orderMatch.setId(orderGroupId);
        }
        if (customerId != null && !customerId.isEmpty()) {
            orderMatch.setCustomerId(customerId);
        }
        orderMatch.setPaymentStatus("PAID");

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "groupOrderMatch: " + orderMatch);
        
        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<OrderGroup> orderExample = Example.of(orderMatch, matcher);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("created").descending());

        Page<OrderGroup> orderWithPage = orderGroupRepository.findAll(getSpecWithDatesBetween(from, to, orderExample), pageable);
          
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderWithPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @GetMapping(path = {"/search"}, name = "orders-group-get-by-id", produces = "application/json")
    @PreAuthorize("hasAnyAuthority('orders-group-get-by-id', 'all')")
    public ResponseEntity<HttpResponse> searchOrderGroup(HttpServletRequest request,
            @RequestParam(required = false) String[] orderGroupIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        
        OrderGroup orderMatch = new OrderGroup();
        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<OrderGroup> orderExample = Example.of(orderMatch, matcher);
        
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("created").descending());
        
        Page<OrderGroup> orderGroup = orderGroupRepository.findAll(getOrderGroupMultipleId(orderGroupIds, orderExample), pageable);
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderGroup);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    /**
     * Accept two dates and example matcher
     *
     * @param from
     * @param to
     * @param example
     * @return
     */
    public Specification<OrderGroup> getSpecWithDatesBetween(
            Date from, Date to, Example<OrderGroup> example) {

        return (Specification<OrderGroup>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("created"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("created"), to));
            }
           
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
    
    
      /**
     * Accept two dates and example matcher
     *
     * @param example
     * @param orderGroupIds
     * @return
     */
    public Specification<OrderGroup> getOrderGroupMultipleId(
            String[] orderGroupIds, Example<OrderGroup> example) {

        return (Specification<OrderGroup>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (orderGroupIds!=null) {
                int idCount = orderGroupIds.length;
                List<Predicate> orderIdPredicatesList = new ArrayList<>();
                for (int i=0;i<orderGroupIds.length;i++) {
                    Predicate predicateForId = builder.equal(root.get("id"), orderGroupIds[i]);
                    orderIdPredicatesList.add(predicateForId);                    
                }

                Predicate finalPredicate = builder.or(orderIdPredicatesList.toArray(new Predicate[idCount]));
                predicates.add(finalPredicate);
            }
                
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "getOrderGroupMultipleId", "Predicates:"+predicates.toString());
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
    
}
