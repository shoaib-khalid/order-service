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
import com.kalsym.order.service.model.repository.CartRepository;
import com.kalsym.order.service.model.repository.StoreDetailsRepository;
import com.kalsym.order.service.model.repository.RegionCountriesRepository;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.service.WhatsappService;
import com.kalsym.order.service.service.CustomerService;
import com.kalsym.order.service.utility.DateTimeUtil;

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
public class ClearEmtpyCartScheduler {
    
    @Autowired
    CartRepository cartRepository;
    
    @Autowired
    RegionCountriesRepository regionCountriesRepository;
    
    @Autowired
    StoreDetailsRepository storeDetailsRepository;
    
    @Autowired
    WhatsappService whatsappService;
    
    @Autowired
    CustomerService customerService;
    
    @Value("${emptycart.scheduler.enabled:false}")
    private boolean isEnabled;
    
    @Scheduled(fixedRate = 300000)
    public void checkEmptyCart() throws Exception {
        /*if (isEnabled) {
            String logprefix = "ClearEmtpyCart-Scheduler"; 
            List<Object[]> cartList = cartRepository.getEmptyCart(items);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Start checking not process order for vertical:"+verticalToSend+". Order Count:"+orderList.size());        
            for (int i=0;i<cartList.size();i++) {
                Object[] cart = cartList.get(i);    
                String cartId = (String)cart[0];
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderId:"+orderId+" storeId:"+storeId+" storeName:"+storeName+" timestamp:"+ts.toString()+" phoneNumber:"+phoneNumber);
                
                //convert time to merchant timezone
                TODO : delete empty cart
            }
        }*/
    }
}
