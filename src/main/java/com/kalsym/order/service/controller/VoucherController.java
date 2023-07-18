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
import com.kalsym.order.service.model.*;
import com.kalsym.order.service.enums.VoucherStatus;
import com.kalsym.order.service.enums.VoucherType;
import com.kalsym.order.service.model.repository.*;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Logger;
import java.util.Optional;
import java.util.List;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;

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
    StoreRepository storeRepository;
    
    @Autowired
    CustomerVoucherRepository customerVoucherRepository;

    @Autowired
    VoucherStoreRepository voucherStoreRepository;

    @Autowired
    VoucherVerticalRepository voucherVerticalRepository;

    @Autowired
    VoucherTermsRepository voucherTermsRepository;

    @Autowired
    VoucherServiceTypeRepository voucherServiceTypeRepository;

    
    @GetMapping(path = {"/available"})
    public ResponseEntity<HttpResponse> getAvailableVoucher(HttpServletRequest request,
            @RequestParam(required = false) VoucherType voucherType,
            @RequestParam(required = false) String verticalCode,
            @RequestParam(required = false) String voucherCode,
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
        
        Specification<Voucher> voucherSpec = VoucherSearchSpecs.getSpecWithDatesBetween(new Date(), voucherType, storeId, verticalCode, voucherCode, example );
        Page<Voucher> voucherWithPage = voucherRepository.findAll(voucherSpec, pageable);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(voucherWithPage);
        
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping(path = {"/all-vouchers"})
    public ResponseEntity<HttpResponse> getAllVouchers(HttpServletRequest request,
            @RequestParam(required = false) VoucherType voucherType,
            @RequestParam(required = false) String verticalCode,
            @RequestParam(required = false) String voucherCode,
            @RequestParam(required = false) String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
            )
    {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logprefix = request.getRequestURI();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "voucherType:" + voucherType+" storeId:"+storeId);

        Voucher voucherMatch = new Voucher();
//        voucherMatch.setStatus(VoucherStatus.ACTIVE);
        Pageable pageable = PageRequest.of(page, pageSize);
        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<Voucher> example = Example.of(voucherMatch, matcher);

        Specification<Voucher> voucherSpec = VoucherSearchSpecs.getVouchersSpec(voucherType, storeId, verticalCode, voucherCode, example );
        Page<Voucher> voucherWithPage = voucherRepository.findAll(voucherSpec, pageable);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(voucherWithPage);

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping(path = {"/all-vouchers/{id}"})
    public ResponseEntity<HttpResponse> getAllVouchersById(HttpServletRequest request,
               @PathVariable String id)
    {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logprefix = request.getRequestURI();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "id:" + id);

        Optional<Voucher> voucherOptional = voucherRepository.findById(id);

        if (!voucherOptional.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "voucher NOT_FOUND: " + id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            response.setError("Voucher not found");
            return ResponseEntity.status(response.getStatus()).body(response);

        }

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(voucherOptional.get());

        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    @GetMapping(path = {"/verify/{customerEmail}/{voucherCode}/{storeId}"}, name = "voucher-post")
    @PreAuthorize("hasAnyAuthority('voucher-post', 'all')")
    public ResponseEntity<HttpResponse> postGuestClaimVoucher(HttpServletRequest request,
            @PathVariable(required = true) String customerEmail,
            @PathVariable(required = true) String voucherCode,
            @PathVariable(required = false) String storeId
            ) {
        String logprefix = request.getRequestURI();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(OrderServiceApplication.VERSION, logprefix, "customerEmail: " + customerEmail+" voucherCode:"+voucherCode);
        
        List<Customer> optCustomer = null;
        if (storeId!=null) {
            optCustomer = customerRepository.findByEmailAndStoreId(customerEmail, storeId);
        } else {
            optCustomer = customerRepository.findByEmail(customerEmail);
        }
        
        //check promo code
        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode);
        if (voucher==null) {
            Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " NOT_FOUND voucherCode: " + voucherCode);
            response.setSuccessStatus(HttpStatus.NOT_FOUND);
            response.setError("Voucher not found");
            response.setMessage("Voucher not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        } else {
            //check status
            if (voucher.getStatus()!=VoucherStatus.ACTIVE) {
                response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
                response.setError("Voucher not active");
                response.setMessage("Voucher not active");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
            //check total redeem
            if (voucher.getTotalRedeem()>=voucher.getTotalQuantity()) {
                response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
                response.setError("Voucher fully redeemed");
                response.setMessage("Sorry, you have redeemed this voucher");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
            //check expiry date
            Date currentDate = new Date();
            if (currentDate.compareTo(voucher.getStartDate()) < 0 || currentDate.compareTo(voucher.getEndDate()) > 0) {
                response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
                response.setError("Voucher is expired");
                response.setMessage("Voucher is expired");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
        }
        
        if (!optCustomer.isEmpty() && !voucher.getAllowMultipleRedeem()) {
            String customerId = optCustomer.get(0).getId();
            CustomerVoucher existingVoucher = customerVoucherRepository.findByCustomerIdAndVoucherId(customerId, voucher.getId());
            if (existingVoucher!=null && existingVoucher.getIsUsed()) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Voucher already exist customerId: " + customerId+" voucherId:"+voucher.getId());
                response.setSuccessStatus(HttpStatus.CONFLICT);
                response.setError("Voucher already exist");
                response.setMessage("Sorry, you have redeemed this voucher");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
        } else if (!voucher.getAllowMultipleRedeem()) {
            //find guest
            List<CustomerVoucher> existingVoucherList = customerVoucherRepository.findByGuestEmailAndVoucherIdAndStoreId(customerEmail, voucher.getId(), storeId);
            if (!existingVoucherList.isEmpty()) {
                CustomerVoucher existingVoucher =existingVoucherList.get(0);
                if (existingVoucher!=null && existingVoucher.getIsUsed()) {
                    Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Voucher already exist email: " + customerEmail+" voucherId:"+voucher.getId());
                    response.setSuccessStatus(HttpStatus.CONFLICT);
                    response.setError("Voucher already exist");
                    response.setMessage("Sorry, you have redeemed this voucher");
                    return ResponseEntity.status(response.getStatus()).body(response);
                }
            }
        }
                
        if (storeId!=null && voucher.getVoucherType()==VoucherType.STORE) {
            //check store id
            boolean storeValid=false;
            List<VoucherStore> storeList = voucher.getVoucherStoreList();
            for (int x=0;x<storeList.size();x++) {
                if (storeId.equals(storeList.get(x).getStoreId())) {
                    storeValid=true;
                    break;
                }
            }
            if (!storeValid) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Voucher not valid for this store storeId: " + storeId+" voucherId:"+voucher.getId());
                response.setSuccessStatus(HttpStatus.CONFLICT);
                response.setError("Voucher not valid for this store");
                response.setMessage("Sorry, voucher code cannot be used for this store");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
        }
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(voucher);
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
        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode);
        if (voucher==null) {
            Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " NOT_FOUND customerId: " + customerId);
            response.setSuccessStatus(HttpStatus.NOT_FOUND);
            response.setError("Voucher not found");
            response.setMessage("Voucher not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        } else {
            //check status
            if (voucher.getStatus()!=VoucherStatus.ACTIVE) {
                response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
                response.setError("Voucher not active");
                response.setMessage("Voucher not active");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
            //check total redeem
            if (voucher.getTotalRedeem()>=voucher.getTotalQuantity()) {
                response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
                response.setError("Voucher fully redeemed");
                response.setMessage("Sorry, voucher is fully redeemed");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
            //check expiry date
            Date currentDate = new Date();
            if (currentDate.compareTo(voucher.getStartDate()) < 0 || currentDate.compareTo(voucher.getEndDate()) > 0) {
                response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
                response.setError("Voucher is expired");
                response.setMessage("Voucher is expired");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
        }
        
        CustomerVoucher existingVoucher = customerVoucherRepository.findByCustomerIdAndVoucherId(customerId, voucher.getId());
        if (existingVoucher!=null && !voucher.getAllowMultipleRedeem()) {
            Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " Voucher already exist customerId: " + customerId+" voucherId:"+voucher.getId());
            response.setSuccessStatus(HttpStatus.CONFLICT);
            response.setError("Voucher already exist");
            response.setMessage("Sorry, you have redeemed this voucher");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        
        //check deactivated account
        List<Customer> deactivatedCustomerList = customerRepository.findByOriginalEmail(optCustomer.get().getEmail());
        for (int i=0;i<deactivatedCustomerList.size();i++) {
            String deactivatedCustomerId = deactivatedCustomerList.get(i).getId();
            existingVoucher = customerVoucherRepository.findByCustomerIdAndVoucherId(deactivatedCustomerId, voucher.getId());
            if (existingVoucher!=null && !voucher.getAllowMultipleRedeem()) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " Voucher already exist customerId: " + customerId+" voucherId:"+voucher.getId());
                response.setSuccessStatus(HttpStatus.CONFLICT);
                response.setError("Voucher already exist");
                response.setMessage("Sorry, you have redeemed this voucher");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
        }
        
        CustomerVoucher customerVoucher = new CustomerVoucher();
        customerVoucher.setCustomerId(customerId);
        customerVoucher.setIsUsed(Boolean.FALSE);
        customerVoucher.setVoucherId(voucher.getId());
        customerVoucher.setCreated(new Date());
        customerVoucher.setGuestVoucher(false);
        CustomerVoucher savedVoucher = customerVoucherRepository.save(customerVoucher);
        
        //refresh and retrieve back the data
        customerVoucherRepository.refresh(savedVoucher);
        Optional<CustomerVoucher> updatedVoucher = customerVoucherRepository.findById(savedVoucher.getId());
        
        response.setSuccessStatus(HttpStatus.CREATED);
        response.setData(updatedVoucher.get());
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    
    
    @GetMapping(path = {"/claim/{customerId}"})
    public ResponseEntity<HttpResponse> getAvailableCustomerVoucher(HttpServletRequest request,            
            @PathVariable(required = true) String customerId,
            @RequestParam(required = false) VoucherType voucherType,
            @RequestParam(required = false) String verticalCode,
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
      
        Pageable pageable = PageRequest.of(page, pageSize);
        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<CustomerVoucher> example = Example.of(customerVoucherMatch, matcher);
        
        Specification voucherSpec = CustomerVoucherSearchSpecs.getSpecWithDatesBetween(new Date(), voucherType, verticalCode, customerId, VoucherStatus.ACTIVE, voucherCode, isUsed, example );
        Page<CustomerVoucher> customerVoucherWithPage = customerVoucherRepository.findAll(voucherSpec, pageable);
        
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(customerVoucherWithPage);
        
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    
    @PostMapping(path = {"/claim/newuser/{customerId}"}, name = "voucher-post")
    @PreAuthorize("hasAnyAuthority('voucher-post', 'all')")
    public ResponseEntity<HttpResponse> postClaimNewUserVoucher(HttpServletRequest request,
            @PathVariable(required = true) String customerId
            ) {
        String logprefix = "postClaimNewUserVoucher()";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(OrderServiceApplication.VERSION, logprefix, "customerId: " + customerId);

        Optional<Customer> optCustomer = customerRepository.findById(customerId);

        if (!optCustomer.isPresent()) {
            Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " NOT_FOUND customerId: " + customerId);
            response.setSuccessStatus(HttpStatus.NOT_FOUND);
            response.setError("customer not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        
        //find 'newuser' voucher code
        Voucher selectedVoucher = null;
        List<Voucher> voucherList = voucherRepository.findAvailableNewUserVoucher(new Date());
        if (voucherList.isEmpty()) {
            Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " Cannot find active voucher. customerId: " + customerId);
            response.setSuccessStatus(HttpStatus.NOT_FOUND);
            response.setError("Voucher not found");
            response.setMessage("Voucher not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        } else {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " Found active voucher:"+voucherList.size()+". customerId: " + customerId);
            Customer customer = optCustomer.get();            
            for (int i=0;i<voucherList.size();i++) {
                Voucher voucher = voucherList.get(i);
                //check region vertical based on customer countrId
                List<VoucherVertical> voucherVerticalList = voucher.getVoucherVerticalList();
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " VoucherId:"+voucher.getId()+". voucherVerticalList: " + voucherVerticalList.size());
                for (int x=0;x<voucherVerticalList.size();x++) {
                    String voucherRegion = voucherVerticalList.get(x).getRegionVertical().getRegionId();
                    String customerRegion = customer.getRegionCountry().getRegion();
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " voucherRegion:[" + voucherRegion +"] customerRegion:["+customerRegion+"]");
                    if (voucherRegion.equalsIgnoreCase(customerRegion)) {
                        selectedVoucher = voucher;
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "SelectedVoucher:"+selectedVoucher.getId());
                    } else {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Wrong region");
                    }
                }                
            }
            
            if (selectedVoucher==null) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " Voucher not found. Check vertical & customer region. customerId: " + customerId);
                response.setSuccessStatus(HttpStatus.NOT_FOUND);
                response.setError("Voucher not found");
                response.setMessage("Voucher not found");
                return ResponseEntity.status(response.getStatus()).body(response);
            } else {
                //check status
                if (selectedVoucher.getStatus()!=VoucherStatus.ACTIVE) {
                    Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " Voucher not active customerId: " + customerId+" voucherId:"+selectedVoucher.getId());
                    response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
                    response.setError("Voucher not active");
                    response.setMessage("Voucher not active");
                    return ResponseEntity.status(response.getStatus()).body(response);
                }            
                //check expiry date
                Date currentDate = new Date();
                if (currentDate.compareTo(selectedVoucher.getStartDate()) < 0 || currentDate.compareTo(selectedVoucher.getEndDate()) > 0) {
                    Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " Voucher is expired customerId: " + customerId+" voucherId:"+selectedVoucher.getId());
                    response.setSuccessStatus(HttpStatus.EXPECTATION_FAILED);
                    response.setError("Voucher is expired");
                    response.setMessage("Voucher is expired");
                    return ResponseEntity.status(response.getStatus()).body(response);
                }
            }
        }
        
        CustomerVoucher existingVoucher = customerVoucherRepository.findByCustomerIdAndVoucherId(customerId, selectedVoucher.getId());
        if (existingVoucher!=null && !selectedVoucher.getAllowMultipleRedeem()) {
            Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " Voucher already exist customerId: " + customerId+" voucherId:"+selectedVoucher.getId());
            response.setSuccessStatus(HttpStatus.CONFLICT);
            response.setError("Voucher already exist");
            response.setMessage("Sorry, you have redeemed this voucher");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        
        //check deactivated account
        List<Customer> deactivatedCustomerList = customerRepository.findByOriginalEmail(optCustomer.get().getEmail());
        for (int i=0;i<deactivatedCustomerList.size();i++) {
            String deactivatedCustomerId = deactivatedCustomerList.get(i).getId();
            existingVoucher = customerVoucherRepository.findByCustomerIdAndVoucherId(deactivatedCustomerId, selectedVoucher.getId());
            if (existingVoucher!=null && !selectedVoucher.getAllowMultipleRedeem()) {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " Voucher already exist customerId: " + customerId+" voucherId:"+selectedVoucher.getId());
                response.setSuccessStatus(HttpStatus.CONFLICT);
                response.setError("Voucher already exist");
                response.setMessage("Sorry, you have redeemed this voucher");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
        }
        
        CustomerVoucher customerVoucher = new CustomerVoucher();
        customerVoucher.setCustomerId(customerId);
        customerVoucher.setIsUsed(Boolean.FALSE);
        customerVoucher.setVoucherId(selectedVoucher.getId());
        customerVoucher.setCreated(new Date());
        customerVoucher.setGuestVoucher(false);
        CustomerVoucher savedVoucher = customerVoucherRepository.save(customerVoucher);
        
        //refresh and retrieve back the data
        customerVoucherRepository.refresh(savedVoucher);
        Optional<CustomerVoucher> updatedVoucher = customerVoucherRepository.findById(savedVoucher.getId());
        
        response.setSuccessStatus(HttpStatus.CREATED);
        response.setData(updatedVoucher.get());
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @ApiOperation(value = "Create voucher", notes = "Note: Include storeId for STORE voucher type.")
    @PostMapping(path = {"/create"}, name = "voucher-post")
    @PreAuthorize("hasAnyAuthority('voucher-post', 'all')")
    public ResponseEntity<HttpResponse> postVoucher(HttpServletRequest request,
         @RequestParam(required = false) String storeId,
         @Valid @RequestBody Voucher voucherBody) throws Exception {

        String logprefix = "postVoucher()";
        HttpResponse response = new HttpResponse(request.getRequestURI());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "bodyProduct: " + voucherBody.toString());

        // Set voucher type to PLATFORM as default
        voucherBody.setVoucherType(VoucherType.PLATFORM);

        // Set total redeem to 0
        voucherBody.setTotalRedeem(0);

        if (storeId != null) {
            Optional<Store> optionalStore = storeRepository.findById(storeId);

            if (!optionalStore.isPresent()) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " NOT_FOUND storeId: " + storeId);
                response.setErrorStatus(HttpStatus.NOT_FOUND);
                response.setError("Store not found");
                return ResponseEntity.status(response.getStatus()).body(response);
            }
            // Set voucher type to STORE whenever has storeId
            voucherBody.setVoucherType(VoucherType.STORE);
        }
        Voucher savedVoucher = voucherRepository.save(voucherBody);

        // If type is STORE, save to voucher_store table
        if (savedVoucher.getVoucherType().equals(VoucherType.STORE) && !voucherBody.getVoucherStoreList().isEmpty()) {
            for (VoucherStore voucherStore: voucherBody.getVoucherStoreList()) {
                voucherStore.setVoucherId((savedVoucher.getId()));

                voucherStoreRepository.save(voucherStore);
            }
        }
        // Save to voucher_service_type table
        if (!voucherBody.getVoucherServiceTypeList().isEmpty()) {
            for (VoucherServiceType voucherServiceType: voucherBody.getVoucherServiceTypeList()) {
                voucherServiceType.setVoucherId(savedVoucher.getId());

                voucherServiceTypeRepository.save(voucherServiceType);
            }
        }

        // Save to voucher_terms table
        if (!voucherBody.getVoucherTerms().isEmpty()) {
            for (VoucherTerms voucherTerms: voucherBody.getVoucherTerms()) {
                voucherTerms.setVoucherId(savedVoucher.getId());

                voucherTermsRepository.save(voucherTerms);
            }
        }

        // Save to voucher_vertical table
        if (!voucherBody.getVoucherVerticalList().isEmpty()) {
            for (VoucherVertical voucherVertical: voucherBody.getVoucherVerticalList()) {
                voucherVertical.setVoucherId(savedVoucher.getId());

                voucherVerticalRepository.save(voucherVertical);
            }
        }

        response.setSuccessStatus(HttpStatus.CREATED);
        response.setData(savedVoucher);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(path = {"/edit/{id}"}, name = "voucher-put")
    @PreAuthorize("hasAnyAuthority('voucher-put', 'all')")
    public ResponseEntity<HttpResponse> putVoucher(HttpServletRequest request,
              @PathVariable String id,
              @RequestBody Voucher bodyVoucher) {
        String logprefix = request.getRequestURI();
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(OrderServiceApplication.VERSION, logprefix, "id: " + id);

        Optional<Voucher> voucherOptional = voucherRepository.findById(id);

        if (!voucherOptional.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " NOT_FOUND voucher with ID: " + id);
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            response.setError("Voucher not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " FOUND voucher with ID: " + id);

        Voucher voucher = voucherOptional.get();

        VoucherType oriVoucherType = voucher.getVoucherType();

        voucher.update(bodyVoucher);
        Voucher updatedVoucher = voucherRepository.save(voucher);

        // Check voucher type, delete data if change type from STORE to PLATFORM
        if (oriVoucherType.equals(VoucherType.STORE) && bodyVoucher.getVoucherType().equals(VoucherType.PLATFORM) ) {
            // Delete data from DB
            voucherStoreRepository.deleteByVoucherId(updatedVoucher.getId());

        }
        // If body - voucher store list exist
        if (!bodyVoucher.getVoucherStoreList().isEmpty() && bodyVoucher.getVoucherType().equals(VoucherType.STORE)) {
            // Delete data from DB
            voucherStoreRepository.deleteByVoucherId(updatedVoucher.getId());

            for (VoucherStore voucherStore: bodyVoucher.getVoucherStoreList()) {
                voucherStore.setVoucherId((updatedVoucher.getId()));

                voucherStoreRepository.save(voucherStore);
            }
        }

        // If exist
        if (!bodyVoucher.getVoucherServiceTypeList().isEmpty()) {
            // Delete data from DB
            voucherServiceTypeRepository.deleteByVoucherId(updatedVoucher.getId());

            // Save to voucher_service_type table
            for (VoucherServiceType voucherServiceType: bodyVoucher.getVoucherServiceTypeList()) {
                voucherServiceType.setVoucherId(updatedVoucher.getId());

                voucherServiceTypeRepository.save(voucherServiceType);
            }
        }

        if (!bodyVoucher.getVoucherTerms().isEmpty()) {
            // Delete data from DB
            voucherTermsRepository.deleteByVoucherId(updatedVoucher.getId());

            // Save to voucher_terms table
            for (VoucherTerms voucherTerms: bodyVoucher.getVoucherTerms()) {
                voucherTerms.setVoucherId(updatedVoucher.getId());

                voucherTermsRepository.save(voucherTerms);
            }
        }

        if (!bodyVoucher.getVoucherVerticalList().isEmpty()) {
            // Delete data from DB
            voucherVerticalRepository.deleteByVoucherId(updatedVoucher.getId());

            // Save to voucher_vertical table
            for (VoucherVertical voucherVertical: bodyVoucher.getVoucherVerticalList()) {
                voucherVertical.setVoucherId(updatedVoucher.getId());

                voucherVerticalRepository.save(voucherVertical);
            }
        }

        response.setData(voucherRepository.findById(id));
        response.setSuccessStatus(HttpStatus.OK);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
