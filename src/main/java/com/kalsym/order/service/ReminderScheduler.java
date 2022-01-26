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
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.service.WhatsappService;
import com.kalsym.order.service.service.CustomerService;

import java.util.List;
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
                String updated = (String)order[7];
                String[] recipients = {phoneNumber};
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
