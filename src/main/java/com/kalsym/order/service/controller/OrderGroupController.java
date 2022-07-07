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
import org.springframework.web.bind.annotation.GetMapping;

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
        //get region country id from one of the order
        String regionCountryId = orderGroup.getOrderList().get(0).getStore().getRegionCountryId();
        orderGroup.setRegionCountryId(regionCountryId);
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(orderGroup);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    

}
