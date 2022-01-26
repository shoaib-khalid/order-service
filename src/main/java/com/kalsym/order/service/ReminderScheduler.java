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

import java.util.List;
import java.util.Date;
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
public class ReminderScheduler {
    
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
    
    @Value("${order.reminder.enabled:false}")
    private boolean isEnabled;
    
    @Scheduled(fixedRate = 300000)
    public void checkNotProcessOrder() throws Exception {
        if (isEnabled) {
            String logprefix = "Reminder-Scheduler";        
            List<Object[]> orderList = orderRepository.getFnBNotProcessOrder();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Start checking not process order. Order Count:"+orderList.size());        
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
                
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderId:"+orderId+" storeId:"+storeId+" storeName:"+storeName+" timestamp:"+ts.toString()+" phoneNumber:"+phoneNumber);
                
                //convert time to merchant timezone
                StoreWithDetails storeWithDetails = null;
                Optional<StoreWithDetails> optStore = storeDetailsRepository.findById(storeId);
                if (!optStore.isPresent()) {
                    storeWithDetails = optStore.get();                
                    RegionCountry regionCountry = null;
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "RegionCountryId:"+storeWithDetails.getRegionCountryId());
                    
                    Optional<RegionCountry> t = regionCountriesRepository.findById(storeWithDetails.getRegionCountryId());
                    if (t.isPresent()) {
                        regionCountry = t.get();                                             
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "RegionCountry:"+regionCountry);
                        LocalDateTime startLocalTime = DateTimeUtil.convertToLocalDateTimeViaInstant(new Date(ts.getTime()), ZoneId.of(regionCountry.getTimezone()) );                
                        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy h:mm a");
                        updated = formatter1.format(startLocalTime);                                    
                    }
                }
                
                //create merchant temp token
                String merchantToken = customerService.GenerateTempToken(clientId, username, password);
                if (merchantToken!=null) {
                    boolean res = whatsappService.sendOrderReminder(recipients, storeName, invoiceId, orderId, merchantToken, updated);
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "OrderId:"+orderId+" InvoiceNo:"+invoiceId+" StoreName:"+storeName+" Reminder result:"+res);
                } else {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "OrderId:"+orderId+" InvoiceNo:"+invoiceId+" StoreName:"+storeName+" Fail to get temp token");
                }
            }
        }
    }
}
