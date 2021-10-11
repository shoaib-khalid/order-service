/*
 * Copyright (C) 2021 taufik
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
package com.kalsym.order.service.filter;

import com.kalsym.order.service.OrderServiceApplication;

import com.kalsym.order.service.model.Product;
import com.kalsym.order.service.model.Order;

import com.kalsym.order.service.model.repository.ProductRepository;
import com.kalsym.order.service.model.repository.StoreRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.Validation;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author taufik
 */
@Component(value="customOwnerVerifier")
public class CustomOwnerVerifier {
 
   @Autowired
   StoreRepository storeRepository;
   
   @Autowired
   ProductRepository productRepository;
   
   @Autowired
   OrderRepository orderRepository;
   
   private final String logPrefix = "CustomOwnerVerifier";
   
   public boolean VerifyProduct(String productId) {
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logPrefix, "VerifyProduct for productId:"+productId, "");
        Optional<Product> optProduct = productRepository.findById(productId);
        if (!optProduct.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logPrefix, "Product Not found", "");
            return false;
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logPrefix, "Product found. StoreId:"+optProduct.get().getStoreId(), "");
         if (!Validation.VerifyStoreId(optProduct.get().getStoreId(), storeRepository)) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logPrefix, "Unathourized productId", "");
            return false;
        }
       
        return true;
    }
   
    public boolean VerifyStore(String storeId) {
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logPrefix, "VerifyStore for storeId:"+storeId, "");
        if (!Validation.VerifyStoreId(storeId, storeRepository)) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logPrefix, "Unathourized storeId", "");
            return false;
        }
       
        return true;
    }
    
    public boolean VerifyCustomer(String customerId) {
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logPrefix, "VerifyCustomer for customerId:"+customerId, "");
        if (!Validation.VerifyCustomerId(customerId)) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logPrefix, "Unathourized customerId", "");
            return false;
        }
       
        return true;
    }
                
    public boolean VerifyOrder(String orderId) {
        //verify order is own by current store token or customer token
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logPrefix, "VerifyOrder for orderId:"+orderId, "");
        Optional<Order> optOrder = orderRepository.findById(orderId);
        if (!optOrder.isPresent()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logPrefix, "Order Not found", "");
            return false;
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logPrefix, "Order found. StoreId:"+optOrder.get().getStoreId(), "");
        if (!Validation.VerifyStoreIdOrCustomerId(optOrder.get().getStoreId(), storeRepository, optOrder.get().getCustomerId() )) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logPrefix, "Unathourized orderId", "");
            return false;
        }
       
        return true;
    }        
            
   
}
