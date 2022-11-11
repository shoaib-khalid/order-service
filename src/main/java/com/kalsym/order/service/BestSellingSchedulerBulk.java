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
import com.kalsym.order.service.model.repository.OrderItemRepository;
import com.kalsym.order.service.model.repository.OrderItemSnapshotRepository;
import com.kalsym.order.service.model.repository.RegionCountriesRepository;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.OrderItemSnapshot;
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
import java.math.BigInteger;
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
public class BestSellingSchedulerBulk {
    
    @Autowired
    OrderItemRepository orderItemRepository;
    
    @Autowired
    OrderItemSnapshotRepository orderSnapshotRepository;
       
    @Value("${bestselling.snapshot.bulk.enabled:true}")
    private boolean isEnabled;
     
    @Scheduled(cron = "0 55 11 * * *")
    public void checkNotProcessOrder() throws Exception {
        
        if (isEnabled) {
            String logprefix = "BestSellingSnapshot-Scheduler"; 
            
             String[] dateList = new String[10];
            dateList[0]="2022-11-01";
            dateList[1]="2022-11-02";
            dateList[2]="2022-11-03";
            dateList[3]="2022-11-04";
            dateList[4]="2022-11-05";
           dateList[5]="2022-11-06";
           dateList[6]="2022-11-07";
           dateList[7]="2022-11-08";
           dateList[8]="2022-11-09";
           dateList[9]="2022-11-10";
           
            for (int x=0;x<dateList.length;x++) {
                String date = dateList[x];
                Date date1=new SimpleDateFormat("yyyy-MM-dd").parse(date);  
                 
                List<Object[]> itemList = orderItemRepository.getTodaySnapshot(date);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Start checking not process order for DINEIN. Order Count:"+itemList.size());        
                for (int i=0;i<itemList.size();i++) {
                    Object[] order = itemList.get(i);
                    BigInteger totalOrder = (BigInteger)order[0];
                    String itemCode = (String)order[1];
                    String productId = (String)order[2];

                    //insert db
                    OrderItemSnapshot snapshot = new OrderItemSnapshot();
                    snapshot.setDt(date1);
                    snapshot.setItemCode(itemCode);
                    snapshot.setProductId(productId);
                    snapshot.setTotalOrder(totalOrder.intValue());
                    orderSnapshotRepository.save(snapshot);

                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Data saved for date:"+date+" ItemCode:"+itemCode+" productId:"+productId+" totalOrder:"+totalOrder);
                }
            }
        }
    }
}
