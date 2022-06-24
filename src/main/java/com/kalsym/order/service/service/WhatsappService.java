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
package com.kalsym.order.service.service;

import com.kalsym.order.service.OrderServiceApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.kalsym.order.service.utility.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author taufik
 */

@Service
public class WhatsappService {
        
    @Value("${whatsapp.service.push.url:https://waw.symplified.it/360dialog/callback/templatemessage/push}")
    private String whatsappServiceUrl;
    
    @Value("${whatsapp.service.order.reminder.templatename:symplified_order_notification}")
    private String orderReminderTemplateName;
    
    @Value("${whatsapp.service.order.reminder.refid:60133429331}")
    private String orderReminderRefId;
    
    @Value("${whatsapp.service.admin.alert.templatename:symplified_admin_alert}")
    private String adminAlertTemplateName;
    
    @Value("${whatsapp.service.admin.alert.refid:60133429331}")
    private String adminAlertRefId;
    
    @Value("${whatsapp.service.admin.msisdn:60133429331,60133731869}")
    private String adminMsisdn;
    
    public boolean sendOrderReminder(String[] recipients, String storeName, String invoiceNo, String orderId, String merchantToken, String updatedTime) throws Exception {
        //alert format : You have new order for store:{{1}} with invoiceNo:{{2}} updated at {{3}}
        String logprefix = "sendWhatsappMessage";
        RestTemplate restTemplate = new RestTemplate();        
        HttpHeaders headers = new HttpHeaders();
        WhatsappMessage request = new WhatsappMessage();
        request.setGuest(false);
        request.setRecipientIds(recipients);
        request.setRefId(recipients[0]);
        request.setReferenceId(orderReminderRefId);
        request.setOrderId(orderId);
        request.setMerchantToken(merchantToken);
        Template template = new Template();
        template.setName(orderReminderTemplateName);
        String[] message = {storeName, invoiceNo, updatedTime};
        template.setParameters(message);
        request.setTemplate(template);
        HttpEntity<WhatsappMessage> httpEntity = new HttpEntity<>(request, headers);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceUrl, "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");

        ResponseEntity<String> res = restTemplate.postForEntity(whatsappServiceUrl, httpEntity, String.class);

        if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
            return true;
        } else {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + res, "");
            return false;
        }

    }
    
    
    public boolean sendAdminAlert(String status, String storeName, String invoiceNo, String orderId, String updatedTime) throws Exception {
        //alert format : Issue category:{{1}} with invoiceNo:{{2}} updated at:{{3}} for store {{4}}
        String logprefix = "sendAdminAlert";
        String[] recipientList = adminMsisdn.split(",");    
        
        ResponseEntity<String> res = null;
        for (int i=0;i<recipientList.length;i++) {
            RestTemplate restTemplate = new RestTemplate();        
            HttpHeaders headers = new HttpHeaders();
            WhatsappMessage request = new WhatsappMessage();
            request.setGuest(false);
            String[] recipients = {recipientList[i]};
            request.setRecipientIds(recipients);
            request.setRefId(recipients[0]);
            request.setReferenceId(adminAlertRefId);
            request.setOrderId(orderId);
            Template template = new Template();
            template.setName(adminAlertTemplateName);
            String[] message = {status, storeName, invoiceNo, updatedTime };
            template.setParameters(message);
            request.setTemplate(template);
            HttpEntity<WhatsappMessage> httpEntity = new HttpEntity<>(request, headers);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceUrl, "");
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");

            res = restTemplate.postForEntity(whatsappServiceUrl, httpEntity, String.class);
        }
        
        if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
            return true;
        } else {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendAdminAlert res: " + res, "");
            return false;
        }

    }
        
    public boolean sendCustomerAlert(String status, String storeName, String invoiceNo, String orderId, String updatedTime, String customerTemplateName, String WATemplateFormat) throws Exception {
        //alert format : %invoiceNo%,%storeName%,%orderStatus%,%timestamp%
        String logprefix = "sendCustomerAlert";
        String[] recipientList = adminMsisdn.split(",");    
        
        ResponseEntity<String> res = null;
        for (int i=0;i<recipientList.length;i++) {
            RestTemplate restTemplate = new RestTemplate();        
            HttpHeaders headers = new HttpHeaders();
            WhatsappMessage request = new WhatsappMessage();
            request.setGuest(false);
            String[] recipients = {recipientList[i]};
            request.setRecipientIds(recipients);
            request.setRefId(recipients[0]);
            request.setReferenceId(adminAlertRefId);
            request.setOrderId(orderId);
            Template template = new Template();
            template.setName(customerTemplateName);
            WATemplateFormat = WATemplateFormat.replaceAll("%invoiceNo%", invoiceNo);
            WATemplateFormat = WATemplateFormat.replaceAll("%storeName%", storeName);
            WATemplateFormat = WATemplateFormat.replaceAll("%orderStatus%", status);
            WATemplateFormat = WATemplateFormat.replaceAll("%timestamp%", updatedTime);
            WATemplateFormat = WATemplateFormat.replaceAll("%orderId%", orderId);
            String[] parameterTypeList = WATemplateFormat.split(";"); 
            for (int x=0;x<parameterTypeList.length;x++) {
                String[] temp=parameterTypeList[x].split("=");
                if (temp[0].equalsIgnoreCase("body")) {
                    String[] parameterList = temp[1].split(",");
                    template.setParameters(parameterList);
                    request.setTemplate(template);
                } else if (temp[0].equalsIgnoreCase("button")) {
                    String[] parameterList = temp[1].split(",");
                    template.setParametersButton(parameterList);
                    request.setTemplateButton(template);
                }
            }
            request.setTemplate(template);
            HttpEntity<WhatsappMessage> httpEntity = new HttpEntity<>(request, headers);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceUrl, "");
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");
            res = restTemplate.postForEntity(whatsappServiceUrl, httpEntity, String.class);
        }
        
        if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
            return true;
        } else {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendCustomerAlert res: " + res, "");
            return false;
        }

    }
}
