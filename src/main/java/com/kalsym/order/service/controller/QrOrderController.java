/*
 * Copyright (C) 2021 mohsi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.kalsym.order.service.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.enums.PaymentStatus;
import com.kalsym.order.service.enums.VoucherStatus;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderWithDetails;
import com.kalsym.order.service.model.OrderItemWithDetails;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.OrderGroup;
import com.kalsym.order.service.model.QrcodeSession;
import com.kalsym.order.service.model.QrcodeOrderGroup;
import com.kalsym.order.service.model.QrcodeGenerateRequest;
import com.kalsym.order.service.model.QrcodeGenerateResponse;
import com.kalsym.order.service.model.QrcodeValidateResponse;
import com.kalsym.order.service.model.Voucher;
import com.kalsym.order.service.model.repository.TagRepository;
import com.kalsym.order.service.model.repository.QrcodeSessionRepository;
import com.kalsym.order.service.model.repository.QrcodeOrderGroupRepository;
import com.kalsym.order.service.model.repository.StoreDetailsRepository;
import com.kalsym.order.service.model.repository.VoucherSearchSpecs;
import com.kalsym.order.service.service.FCMService;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.TxIdUtil;
import java.util.List;
import java.util.Optional;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author mohsin
 */
@RestController
@RequestMapping("/qrorder")
public class QrOrderController {

   
    @Autowired
    TagRepository tagRepository;
    
    @Autowired
    QrcodeSessionRepository qrcodeSessionRepository;
    
    @Autowired
    QrcodeOrderGroupRepository qrcodeOrderGroupRepository;
    
    @Autowired
    StoreDetailsRepository storeDetailsRepository;
    
    @Autowired
    FCMService fcmService;
    
    @Value("${qrcode.URL:https://dev-my2.symplified.ai/getting-started}")
    String qrCodeUrl;
    
    @GetMapping(path = {"/search"}, name = "qrorder-search")
    public ResponseEntity<HttpResponse> search(HttpServletRequest request, 
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String tableNo,
            @RequestParam(required = false) String invoiceNo,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) String[] orderGroupIds,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,            
            @RequestParam(required = false, defaultValue = "created") String sortByCol,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortingOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
            ) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "qrorder-search-get, URL:  " + request.getRequestURI());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request param:  storeId=" + storeId+" tableNo="+tableNo+" invoiceNo="+invoiceNo+" paymentStatus="+paymentStatus);
       
        QrcodeOrderGroup orderMatch = new QrcodeOrderGroup();
        if (storeId!=null) {
            orderMatch.setStoreId(storeId);       
        }
        if (tableNo!=null) {
            orderMatch.setTableNo(tableNo);       
        }
        if (invoiceNo!=null) {
            orderMatch.setInvoiceNo(invoiceNo);       
        }
        if (paymentStatus!=null) {
            orderMatch.setPaymentStatus(paymentStatus);       
        }
        Pageable pageable = PageRequest.of(page, pageSize);
        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<QrcodeOrderGroup> example = Example.of(orderMatch, matcher);
                
        Page<QrcodeOrderGroup> orderWithPage = qrcodeOrderGroupRepository.findAll(getSpecWithDatesBetweenMultipleStatus(from, to, orderGroupIds, example), pageable);
        
        //consolidate item
        List<QrcodeOrderGroup> orderList = orderWithPage.getContent();
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderWithPage);
        
        return ResponseEntity.status(response.getStatus()).body(response);
        
    }
    
    
    @GetMapping(path = {"/pending"}, name = "qrorder-pending")
    public ResponseEntity<HttpResponse> pending(HttpServletRequest request, 
            @RequestParam(required = true) String storeId,
            @RequestParam(required = true) String tableNo,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortingOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
            ) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "qrorder-search-get, URL:  " + request.getRequestURI());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request param:  storeId=" + storeId+" tableNo="+tableNo);
       
        QrcodeOrderGroup orderMatch = new QrcodeOrderGroup();
        orderMatch.setStoreId(storeId);               
        orderMatch.setTableNo(tableNo);                       
        orderMatch.setPaymentStatus(PaymentStatus.PENDING);       
        
        Pageable pageable = PageRequest.of(page, pageSize);
        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<QrcodeOrderGroup> example = Example.of(orderMatch, matcher);
                
        Page<QrcodeOrderGroup> orderWithPage = qrcodeOrderGroupRepository.findAll(getSpecWithDatesBetweenMultipleStatus(null, null, null, example), pageable);
        
        //consolidate item
        List<QrcodeOrderGroup> qrOrderList = orderWithPage.getContent();
        
        /*OrderDetails[] orderDetailsList = new OrderDetails[orderList.size()];
        
        for (int i=0;i<qrOrderList.size();i++) {
            QrcodeOrderGroup qrOrder = qrOrderList.get(i);
            List<OrderGroup> orderGroupList = qrOrder.getOrderGroupList();
            for (int x=0;x<orderGroupList.size();x++) {
                OrderGroup orderGroup = orderGroupList.get(x);
                List<OrderWithDetails> orderList = orderGroup.getOrderList();
                for (int z=0;z<orderList.size();z++) {
                    OrderWithDetails orderWithDetails = orderList.get(z);
                    List<OrderItemWithDetails> orderItem = orderWithDetails.getOrderItemWithDetails();
                    orderItem
                }
            }
            OrderDetails orderDetails = new OrderDetails();
            orderDetails.setOrder(order);            
            orderDetails.setCurrentCompletionStatus(order.getCompletionStatus().name());
        
            Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(order.getStoreId());
            StoreWithDetails storeWithDetails = optStore.get();
            String verticalId = storeWithDetails.getVerticalCode();
            Boolean storePickup = order.getOrderShipmentDetail().getStorePickup();
            String storeDeliveryType = storeWithDetails.getStoreDeliveryDetail().getType();
            if (order.getServiceType()!=null && order.getServiceType()==ServiceType.DINEIN) {
                storeDeliveryType = order.getDineInOption().name();
            }
        }*/
        
        //calculate grand total 
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderWithPage);
        
        return ResponseEntity.status(response.getStatus()).body(response);
        
    }
    
    
    @PutMapping(path = {"/update"}, name = "qrorder-update")
    public ResponseEntity<HttpResponse> update(HttpServletRequest request, 
            @RequestBody QrcodeOrderGroup bodyOrder
            ) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "qrorder-update-put, URL:  " + request.getRequestURI());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request tableNo:  " + bodyOrder.getTableNo());
        
        QrcodeOrderGroup order = qrcodeOrderGroupRepository.findByStoreIdAndTableNoAndPaymentStatus(bodyOrder.getStoreId(), bodyOrder.getTableNo(), PaymentStatus.PENDING);
        if (order==null) {
            //order not found
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "QrOrder with token: " + bodyOrder.getQrToken() + " not found");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("Order not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
       
        //update paymentStatus to paid
        order.setPaymentStatus(PaymentStatus.PAID);
        order = qrcodeOrderGroupRepository.save(order);
        response.setData(order);
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    
    /**
     * Accept two dates and example matcher
     *
     * @param from
     * @param to
     * @param orderGroupIds
     * @param example
     * @return
     */
    public Specification<QrcodeOrderGroup> getSpecWithDatesBetweenMultipleStatus(
            Date from, Date to, String[] orderGroupIds, Example<QrcodeOrderGroup> example) {

        return (Specification<QrcodeOrderGroup>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            Join<QrcodeOrderGroup, OrderGroup> orderGroupList = root.join("orderGroupList", JoinType.INNER);
            
            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("created"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("created"), to));
            }
                        
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));
            
            if (orderGroupIds!=null) {
                int idCount = orderGroupIds.length;
                List<Predicate> orderIdPredicatesList = new ArrayList<>();
                for (int i=0;i<orderGroupIds.length;i++) {
                    Predicate predicateForId = builder.equal(orderGroupList.get("id"), orderGroupIds[i]);
                    orderIdPredicatesList.add(predicateForId);                    
                }

                Predicate finalPredicate = builder.or(orderIdPredicatesList.toArray(new Predicate[idCount]));
                predicates.add(finalPredicate);
            }
            query.distinct(true);
            
            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }
    
}
