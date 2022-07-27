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
package com.kalsym.order.service;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.repository.CartItemRepository;
import com.kalsym.order.service.model.repository.StoreDiscountRepository;
import com.kalsym.order.service.model.repository.ProductInventoryRepository;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.model.ProductInventory;
import com.kalsym.order.service.model.CartItem;
import com.kalsym.order.service.model.object.ItemDiscount;
import com.kalsym.order.service.enums.DiscountCalculationType;
import com.kalsym.order.service.utility.ProductDiscount;

import java.util.List;
import java.util.Date;
import java.util.Arrays;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Value;
/**
 *
 * @author taufik
 */

@Component
public class ProductDiscountScheduler {
    
    @Autowired
    CartItemRepository cartItemRepository;
    
    @Autowired
    StoreDiscountRepository storeDiscountRepository;
    
    @Autowired
    ProductInventoryRepository productInventoryRepository;
   
    @Value("${productdiscount.scheduler.enabled:false}")
    private boolean isEnabled;
    
    @Scheduled(fixedRate = 300000)
    public void checkCartItem() throws Exception {
        if (isEnabled) {
            String logprefix = "ProductDiscount-Scheduler"; 
            List<Object[]> itemList = cartItemRepository.getCartItemWithDiscount();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Start checking cart item with discount. Item Count:"+itemList.size());        
            for (int i=0;i<itemList.size();i++) {
                Object[] item = itemList.get(i);    
                String itemId = (String)item[0];
                String itemCode = (String)item[1];
                String storeId = (String)item[2];
                
                ProductInventory productInventory = productInventoryRepository.findByItemCode(itemCode);
                
                //check current price
                ItemDiscount discountDetails = ProductDiscount.getItemDiscount(storeDiscountRepository, storeId, itemCode);
                if (discountDetails != null) {                    
                    double discountedPrice = productInventory.getPrice();
                    if (discountDetails.calculationType.equals(DiscountCalculationType.FIX)) {
                        discountedPrice = productInventory.getPrice() - discountDetails.discountAmount;
                    } else if (discountDetails.calculationType.equals(DiscountCalculationType.PERCENT)) {
                        discountedPrice = productInventory.getPrice() - (discountDetails.discountAmount / 100 * productInventory.getPrice());
                    }
                    discountDetails.discountedPrice = discountedPrice;
                    discountDetails.normalPrice = productInventory.getPrice();                    
                    productInventory.setItemDiscount(discountDetails); 
                    
                    double itemProductPrice = productInventory.getPrice();
                            
                    //update price
                    Optional<CartItem> cartItemOpt = cartItemRepository.findById(itemId);
                    CartItem cartItem = cartItemOpt.get();
                    cartItem.setProductPrice((float)discountDetails.discountedPrice);
                    cartItem.setPrice((float)(cartItem.getQuantity() * discountDetails.discountedPrice));
                    cartItem.setDiscountId(discountDetails.discountId);
                    cartItem.setNormalPrice((float)discountDetails.normalPrice);
                    cartItem.setDiscountLabel(discountDetails.discountLabel);
                    cartItem.setDiscountCheckTimestamp(new Date());
                    cartItemRepository.save(cartItem);
                    
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Discounted item updated item:"+itemCode+" discountId:"+discountDetails.discountId);
                } else {
                    //update original price                      
                    Optional<CartItem> cartItemOpt = cartItemRepository.findById(itemId);
                    CartItem cartItem = cartItemOpt.get();
                    double itemPrice = productInventory.getPrice();
                    cartItem.setProductPrice((float)itemPrice);
                    cartItem.setPrice((float)(cartItem.getQuantity() * itemPrice));
                    cartItem.setDiscountId(null);
                    cartItem.setNormalPrice(null);
                    cartItem.setDiscountLabel(null);
                    cartItemRepository.save(cartItem);
                    
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Discounted item removed item:"+itemCode);
                }
                                               
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "item:"+itemCode+" updated");
            }
        }
    }
}
