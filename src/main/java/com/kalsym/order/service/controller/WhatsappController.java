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
import com.kalsym.order.service.model.Voucher;
import com.kalsym.order.service.model.Customer;
import com.kalsym.order.service.model.CustomerVoucher;
import com.kalsym.order.service.model.VoucherVertical;
import com.kalsym.order.service.enums.VoucherStatus;
import com.kalsym.order.service.enums.VoucherType;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderCompletionStatusUpdate;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.repository.VoucherSearchSpecs;
import com.kalsym.order.service.model.repository.VoucherRepository;
import com.kalsym.order.service.model.repository.CustomerRepository;
import com.kalsym.order.service.model.repository.CustomerVoucherRepository;
import com.kalsym.order.service.model.repository.CustomerVoucherSearchSpecs;
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.model.repository.StoreDetailsRepository;
import com.kalsym.order.service.model.repository.RegionCountriesRepository;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.DateTimeUtil;
import com.kalsym.order.service.service.WhatsappService;
import com.kalsym.order.service.service.whatsapp.*;
import java.util.Optional;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author mohsin
 */
@RestController
@RequestMapping("/whatsapp")
public class WhatsappController {

    @Autowired
    VoucherRepository voucherRepository;
    
    @Autowired
    CustomerRepository customerRepository;
    
    @Autowired
    CustomerVoucherRepository customerVoucherRepository;   
    
    @Autowired
    WhatsappService whatsappService;
    
    @Autowired
    OrderRepository orderRepository;
     
    @Autowired
    OrderItemRepository orderItemRepository;
    
    @Autowired
    StoreDetailsRepository storeDetailsRepository;
    
    @Autowired
    RegionCountriesRepository regionCountriesRepository;
    
    @Value("${whatsapp.process.order.URL:https://api.symplified.it/order-service/v1/orders/%orderId%/completion-status-updates}")
    private String processOrderUrl;
    
    @PostMapping(path = {"/receive"}, name = "webhook-post")
    public ResponseEntity<HttpResponse> webhook(HttpServletRequest request, @RequestBody String json) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "callback-message-get, URL:  " + request.getRequestURI());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request Body:  " + json);
        
        JsonObject jsonResp = new Gson().fromJson(json, JsonObject.class);
        JsonObject entry = jsonResp.get("entry").getAsJsonArray().get(0).getAsJsonObject();
        JsonObject changes = entry.get("changes").getAsJsonArray().get(0).getAsJsonObject();
        
        JsonObject messages = null;
        try {
            messages = changes.get("value").getAsJsonObject().get("messages").getAsJsonArray().get(0).getAsJsonObject();
        } catch (Exception ex) {
            //not a message
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "callback-message-get, not a message");            
            JsonObject statuses = changes.get("value").getAsJsonObject().get("statuses").getAsJsonArray().get(0).getAsJsonObject();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "callback-message-get, receive a status : "+statuses.toString());            
            response.setSuccessStatus(HttpStatus.OK);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "callback-message-get, MessageBody: " + messages);
        
        JsonObject context = null;
        try {
            context = messages.get("context").getAsJsonObject();            
        } catch (Exception ex) {}
        
        WhatsappMessage messageBody = new WhatsappMessage();
        String phone = null;
        String userInput = null;
        String type = null;
        String replyTitle=null;
        String replyId=null;
        
        if (context!=null) {
            //user reply
            phone = messages.get("from").getAsString();
            type = messages.get("type").getAsString();
            if (type.equals("interactive")) {
                JsonObject interactive = messages.get("interactive").getAsJsonObject();
                String interactiveType = interactive.get("type").getAsString();
                if (interactiveType.equals("list_reply")) {
                    JsonObject listReply = interactive.get("list_reply").getAsJsonObject();
                    replyId = listReply.get("id").getAsString();
                    replyTitle = listReply.get("title").getAsString();
                } else if (interactiveType.equals("button_reply")) {
                    JsonObject listReply = interactive.get("button_reply").getAsJsonObject();
                    replyId = listReply.get("id").getAsString();
                    replyTitle = listReply.get("title").getAsString();
                }
            } else if (type.equals("button")) {
                JsonObject button = messages.get("button").getAsJsonObject();
                replyId = button.get("payload").getAsString();
                replyTitle = button.get("text").getAsString();
            }            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Incoming message. Msisdn:"+phone+" UserReply: " + replyId+" -> "+replyTitle);        
        } else {
            //user input
            type = "input";
            phone = messages.get("from").getAsString();
            userInput = messages.get("text").getAsJsonObject().get("body").getAsString();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Incoming message. Msisdn:"+phone+" UserInput:" + userInput);        
        }
        
        //route to whatsapp-java-service
        if (replyId!=null && !replyId.equals("")) {
            String[] temp = replyId.split(",");
            String replyAction = temp[0];
            String orderId = temp[1];
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Reply Action:"+replyAction+" OrderId:"+orderId);        
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {  
                //send error
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Order not found");
                Order dummyOrder = new Order();
                dummyOrder.setId(orderId);
                dummyOrder.setInvoiceId(orderId);
                String[] recipientList = {phone};
                whatsappService.sendNotification(recipientList, dummyOrder, "Order not found for orderId:"+orderId);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            String[] recipientList = {phone};
            Order order = orderOpt.get();
            if (replyAction.contains("ORDER_VIEW")) {
               //view the order
                List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);                
                String orderTime = ConvertOrderTimeToStoreTimeZone(order.getStoreId(), orderOpt.get().getCreated(), logprefix);
                whatsappService.sendViewOrderResponse(recipientList, order, orderItems, orderTime);
            } else if (replyAction.contains("ORDER_REJECT")) {
                //cancel order
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Merchant cancel the order. OrderId:"+orderId);        
                boolean res = ProcessOrder(orderId, "CANCEL", logprefix);
                if (res) {
                    whatsappService.sendNotification(recipientList, order, "Order have been canceled for invoiceNo:"+order.getInvoiceId());
                } else {
                    //fail to cancel, resend to cancel
                    whatsappService.sendRetryCancel(recipientList, order, "Fail to cancel order for invoiceNo:"+order.getInvoiceId()+". Click button below to retry");
                }
            } else if (replyAction.contains("ORDER_PROCESS")) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Merchant process the order. OrderId:"+orderId);        
                boolean res = ProcessOrder(orderId, "PROCESS", logprefix);
                if (res) {
                    whatsappService.sendNotification(recipientList, order, "Order has been processed for :"+order.getInvoiceId());
                } else {
                    //fail to process, resend to process
                    whatsappService.sendRetryProcess(recipientList, order, "Fail to process order for invoiceNo:"+order.getInvoiceId()+". Click button below to retry");
                }
            }
            
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    private String ConvertOrderTimeToStoreTimeZone(String storeId, Date orderCreated, String logprefix) {
        String orderTime = null;
        StoreWithDetails storeWithDetails = null;
        Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(storeId);
        if (optStore.isPresent()) {
            storeWithDetails = optStore.get();                
            RegionCountry regionCountry = null;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "RegionCountryId:"+storeWithDetails.getRegionCountryId());

            Optional<RegionCountry> t = regionCountriesRepository.findById(storeWithDetails.getRegionCountryId());
            if (t.isPresent()) {
                regionCountry = t.get();                                             
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "RegionCountry:"+regionCountry);
                LocalDateTime startLocalTime = DateTimeUtil.convertToLocalDateTimeViaInstant(orderCreated, ZoneId.of(regionCountry.getTimezone()) );                
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy h:mm a");
                orderTime = formatter1.format(startLocalTime);                                    
            }
        } else {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "StoreWithDetails not found");
        }
        return orderTime;
    }
    
    private boolean ProcessOrder(String orderId, String action, String logprefix) {
        
        OrderCompletionStatusUpdate request = new OrderCompletionStatusUpdate();
        request.setOrderId(orderId);
        
        if (action.equals("CANCEL")) {
            request.setStatus(OrderStatus.CANCELED_BY_MERCHANT);
        } else if (action.equals("PROCESS")) {
            request.setStatus(OrderStatus.BEING_PREPARED);
        }
        
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");
         
        HttpEntity<OrderCompletionStatusUpdate> httpEntity = new HttpEntity<>(request, headers);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + processOrderUrl, "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");
        
        try {
            ResponseEntity<String> res = restTemplate.postForEntity(processOrderUrl, httpEntity, String.class);

            if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
                return true;
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + res, "");
                return false;
            }
        
        } catch (Exception ex) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + ex.getMessage(), "");
            return false;
        }
    }

}
