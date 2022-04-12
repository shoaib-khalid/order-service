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

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.Voucher;
import com.kalsym.order.service.model.Customer;
import com.kalsym.order.service.model.CustomerVoucher;
import com.kalsym.order.service.enums.VoucherStatus;
import com.kalsym.order.service.enums.VoucherType;
import com.kalsym.order.service.model.repository.VoucherSearchSpecs;
import com.kalsym.order.service.model.repository.VoucherRepository;
import com.kalsym.order.service.model.repository.CustomerRepository;
import com.kalsym.order.service.model.repository.CustomerVoucherRepository;
import com.kalsym.order.service.model.repository.CustomerVoucherSearchSpecs;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Logger;
import java.util.Optional;
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

/**
 *
 * @author mohsin
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Autowired
    VoucherRepository voucherRepository;
    
    @Autowired
    CustomerRepository customerRepository;
    
    @Autowired
    CustomerVoucherRepository customerVoucherRepository;    
    
    @GetMapping(path = {"/available"})
    public ResponseEntity<HttpResponse> getAvailableVoucher(HttpServletRequest request,
            @RequestParam(required = false) VoucherType voucherType,
            @RequestParam(required = false) String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
            )
    {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logprefix = request.getRequestURI();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "voucherType:" + voucherType+" storeId:"+storeId);
      
        Voucher voucherMatch = new Voucher();
        voucherMatch.setStatus(VoucherStatus.ACTIVE);
        Pageable pageable = PageRequest.of(page, pageSize);
        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<Voucher> example = Example.of(voucherMatch, matcher);
        
        Specification voucherSpec = VoucherSearchSpecs.getSpecWithDatesBetween(new Date(), voucherType, storeId, example );
        Page<Voucher> voucherWithPage = voucherRepository.findAll(voucherSpec, pageable);
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(voucherWithPage);
        
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    
    @PostMapping(path = {"/claim/{customerId}/{voucherCode}"}, name = "voucher-post")
    @PreAuthorize("hasAnyAuthority('voucher-post', 'all')")
    public ResponseEntity<HttpResponse> postCustomerClaimVoucher(HttpServletRequest request,
            @PathVariable(required = true) String customerId,
            @PathVariable(required = true) String voucherCode
            ) {
        String logprefix = request.getRequestURI();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(OrderServiceApplication.VERSION, logprefix, "customerId: " + customerId+" voucherCode:"+voucherCode);

        Optional<Customer> optCustomer = customerRepository.findById(customerId);

        if (!optCustomer.isPresent()) {
            Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " NOT_FOUND customerId: " + customerId);
            response.setSuccessStatus(HttpStatus.NOT_FOUND);
            response.setError("customer not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        
        //check promo code
        Voucher voucher = voucherRepository.findAvailableVoucherByCode(voucherCode, new Date());
        if (voucher==null) {
            //check expired date
            Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " NOT_FOUND customerId: " + customerId);
            response.setSuccessStatus(HttpStatus.NOT_FOUND);
            response.setError("voucher not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        
        CustomerVoucher customerVoucher = new CustomerVoucher();
        customerVoucher.setCustomerId(customerId);
        customerVoucher.setIsUsed(Boolean.FALSE);
        customerVoucher.setVoucherId(voucher.getId());
        customerVoucher.setCreated(new Date());
        customerVoucherRepository.save(customerVoucher);
        response.setSuccessStatus(HttpStatus.CREATED);
        response.setData(customerVoucher);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    
    @GetMapping(path = {"/claim/{customerId}"})
    public ResponseEntity<HttpResponse> getAvailableCustomerVoucher(HttpServletRequest request,            
            @PathVariable(required = true) String customerId,
            @RequestParam(required = false) VoucherType voucherType,
            @RequestParam(required = false) String voucherCode,
            @RequestParam(required = false) Boolean isUsed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
            )
    {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logprefix = request.getRequestURI();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "customerId:"+customerId+" voucherType:" + voucherType);
      
        CustomerVoucher customerVoucherMatch = new CustomerVoucher();
        //customerVoucherMatch.setVoucher(voucher);
        //customerVoucherMatch.getVoucher().setStatus(VoucherStatus.ACTIVE);
        Pageable pageable = PageRequest.of(page, pageSize);
        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<CustomerVoucher> example = Example.of(customerVoucherMatch, matcher);
        
        Specification voucherSpec = CustomerVoucherSearchSpecs.getSpecWithDatesBetween(new Date(), voucherType, customerId, VoucherStatus.ACTIVE, voucherCode, isUsed, example );
        Page<CustomerVoucher> customerVoucherWithPage = customerVoucherRepository.findAll(voucherSpec, pageable);
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(customerVoucherWithPage);
        
        return ResponseEntity.status(response.getStatus()).body(response);
    }


}
