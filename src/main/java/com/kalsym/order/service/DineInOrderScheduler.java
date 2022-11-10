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
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.model.repository.StoreDetailsRepository;
import com.kalsym.order.service.model.repository.RegionCountriesRepository;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.service.WhatsappService;
import com.kalsym.order.service.service.CustomerService;
import com.kalsym.order.service.utility.DateTimeUtil;
import com.kalsym.order.service.utility.OrderWorker;

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
import org.springframework.messaging.simp.SimpMessagingTemplate;
/**
 *
 * @author taufik
 */

@Component
public class DineInOrderScheduler {
    
    @Autowired
    OrderRepository orderRepository;
    
    @Autowired
    RegionCountriesRepository regionCountriesRepository;
    
    @Autowired
    StoreDetailsRepository storeDetailsRepository;
    
    @Autowired
    WhatsappService whatsappService;
    
    @Autowired
    CustomerService customerService;
    
    @Value("${dinein.autocancel.enabled:false}")
    private boolean isEnabled;
  
    @Value("${dinein.autocancel.wait.hour:24}")
    private int hourToWait;
    
    @Value("${whatsapp.process.order.URL:https://api.symplified.it/order-service/v1/orders/%orderId%/completion-status-updates}")
    String processOrderUrl;
      
    @Scheduled(fixedRate = 60000)
    public void checkNotProcessOrder() throws Exception {
        
        if (isEnabled) {
            String logprefix = "AutoCancel-Scheduler"; 
            List<Object[]> orderList = orderRepository.getNotProcessOrderDineIn(hourToWait);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Start checking not process order for DINEIN. Order Count:"+orderList.size());        
            for (int i=0;i<orderList.size();i++) {
                Object[] order = orderList.get(i);
                String orderId = (String)order[0];
                String invoiceId = (String)order[1];
                String phoneNumber = (String)order[2];
                String storeName = (String)order[3];
                String clientId = (String)order[4];
                String username = (String)order[5];
                String password = (String)order[6];
                Timestamp ts = (java.sql.Timestamp)order[7];
                String storeId = (String)order[8];
                String[] recipients = {phoneNumber};
                String updated = null;   
                
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "CANCEL ORDER for orderId:"+orderId+" storeId:"+storeId+" storeName:"+storeName+" timestamp:"+ts.toString()+" phoneNumber:"+phoneNumber);
                
                //cancel order
                boolean res = OrderWorker.ProcessOrder(orderId, "CANCEL", logprefix, processOrderUrl);
               
            }
        }
    }
}