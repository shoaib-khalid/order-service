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

import com.kalsym.order.service.service.whatsapp.WhatsappMessage;
import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderSubItem;
import com.kalsym.order.service.service.whatsapp.Action;
import com.kalsym.order.service.service.whatsapp.Body;
import com.kalsym.order.service.service.whatsapp.Button;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.Utilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kalsym.order.service.service.whatsapp.WhatsappInteractiveMessage;
import com.kalsym.order.service.service.whatsapp.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author taufik
 */

@Service
public class WhatsappService {
        
    @Value("${whatsapp.service.template.url:https://api.symplified.it/whatsapp-java-service/v1/templatemessage/push}")
    private String whatsappServiceUrl;
    
    @Value("${whatsapp.service.interactive.url:https://api.symplified.it/whatsapp-java-service/v1/interactive/push}")
    private String whatsappServiceInteractiveUrl;
    
    @Value("${whatsapp.service.notification.url:https://api.symplified.it/whatsapp-java-service/v1/interactive/notification}")
    private String whatsappServiceNotificationUrl;
    
    @Value("${whatsapp.service.order.reminder.templatename:deliverin_process_new_order3}")
    private String orderReminderTemplateName;
    
    @Value("${whatsapp.service.order.awaiting.pickup.templatename:deliverin_process_awaiting_pickup}")
    private String orderAwaitingPickupTemplateName;
    
    @Value("${whatsapp.service.order.awaiting.selfpickup.templatename:deliverin_process_awaiting_selfpickup}")
    private String orderAwaitingSelfPickupTemplateName;
    
    @Value("${whatsapp.service.copy.reminder.templatename:symplified_new_order_notification}")
    private String copyOrderReminderTemplateName;
    
    @Value("${whatsapp.service.order.reminder.refid:60133429331}")
    private String orderReminderRefId;
    
    @Value("${whatsapp.service.admin.alert.templatename:symplified_admin_alert}")
    private String adminAlertTemplateName;
    
    @Value("${whatsapp.service.admin.alert.refid:60133429331}")
    private String adminAlertRefId;
    
    @Value("${whatsapp.service.admin.msisdn:60133429331,60133731869}")
    private String adminMsisdn;
    
    @Value("${whatsapp.button.reply.prefix:STG}")
    private String orderButtonReplyPrefix;
    
    public boolean sendOrderReminderMerchant(String[] recipients, String storeName, String invoiceNo, String orderId, String merchantToken, String updatedTime) throws Exception {
        //alert format : You have new order for store:{{1}} with invoiceNo:{{2}} updated at {{3}}
        String logprefix = "sendOrderReminder";
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
        
        ButtonParameter[] buttonParameters = new ButtonParameter[2];
        ButtonParameter buttonParameter1 = new ButtonParameter();
        buttonParameter1.setIndex(0);
        buttonParameter1.setSub_type("quick_reply");
        String[] params = {orderButtonReplyPrefix+"_ORDER_VIEW,"+orderId};
        buttonParameter1.setParameters(params);
        buttonParameters[0] = buttonParameter1;
        ButtonParameter buttonParameter2 = new ButtonParameter();
        buttonParameter2.setIndex(1);
        buttonParameter2.setSub_type("quick_reply");
        String[] params2 = {orderButtonReplyPrefix+"_ORDER_REJECT,"+orderId};
        buttonParameter2.setParameters(params2);
        buttonParameters[1] = buttonParameter2;
        template.setButtonParameters(buttonParameters);
        
        String[] headerParam = {invoiceNo};
        template.setParametersHeader(headerParam);
        
        request.setTemplate(template);
        HttpEntity<WhatsappMessage> httpEntity = new HttpEntity<>(request, headers);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceUrl, "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");
        
        try {
            ResponseEntity<String> res = restTemplate.postForEntity(whatsappServiceUrl, httpEntity, String.class);

            if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
                return true;
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + res, "");
                return false;
            }
        
        } catch (Exception ex) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + ex.getMessage(), "");
            return false;
        }

    }
    
    
    public boolean sendOrderReminderCopy(String[] recipients, String storeName, String invoiceNo, String orderId, String merchantToken, String updatedTime) throws Exception {
        //alert format : You have new order for store:{{1}} with invoiceNo:{{2}} updated at {{3}}
        String logprefix = "sendCopyOrderReminder";
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
        template.setName(copyOrderReminderTemplateName);
        String[] message = {storeName, invoiceNo, updatedTime};
        template.setParameters(message);
        request.setTemplate(template);
        HttpEntity<WhatsappMessage> httpEntity = new HttpEntity<>(request, headers);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceUrl, "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");
        
        try {
            ResponseEntity<String> res = restTemplate.postForEntity(whatsappServiceUrl, httpEntity, String.class);

            if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
                return true;
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + res, "");
                return false;
            }
        
        } catch (Exception ex) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + ex.getMessage(), "");
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
            
            try {
                res = restTemplate.postForEntity(whatsappServiceUrl, httpEntity, String.class);
            
            } catch (Exception ex) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendAdminAlert res: " + ex.getMessage(), "");
                
            }
        }
        
        
        if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
            return true;
        } else {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendAdminAlert res: " + res, "");
            return false;
        }
        
       

    }
        
    public boolean sendCustomerAlert(
            String customerMsisdn, String status, String storeName, 
            String invoiceNo, String orderId, String updatedTime,
            String customerTemplateName, String WATemplateFormat, String storeCity, 
            String invoicePdf, boolean isRegisteredUser) throws Exception {
        //alert format : %invoiceNo%,%storeName%,%orderStatus%,%timestamp%
        String logprefix = "sendCustomerAlert";
        
        ResponseEntity<String> res = null;
        RestTemplate restTemplate = new RestTemplate();        
        HttpHeaders headers = new HttpHeaders();
        WhatsappMessage request = new WhatsappMessage();
        request.setGuest(false);
        String[] recipients = {customerMsisdn};
        request.setRecipientIds(recipients);
        request.setRefId(recipients[0]);
        request.setReferenceId(adminAlertRefId);
        request.setOrderId(orderId);
        
        
        Template template = new Template();
        String templateNameGuest = customerTemplateName;
        String templateNameUser = customerTemplateName;
        String[] templateNameList = customerTemplateName.split(";"); 
        for (int z=0;z<templateNameList.length;z++) {
            String[] temp=templateNameList[z].split("=");
            if (temp[0].equalsIgnoreCase("guest")) {
                templateNameGuest = temp[1];
            } else if (temp[0].equalsIgnoreCase("user")) {
                templateNameUser = temp[1];
            }
        }
        if (isRegisteredUser)
            template.setName(templateNameUser);
        else
            template.setName(templateNameGuest);
        
        WATemplateFormat = WATemplateFormat.replaceAll("%invoiceNo%", invoiceNo);
        WATemplateFormat = WATemplateFormat.replaceAll("%storeName%", storeName);
        WATemplateFormat = WATemplateFormat.replaceAll("%orderStatus%", status);
        WATemplateFormat = WATemplateFormat.replaceAll("%timestamp%", updatedTime);
        WATemplateFormat = WATemplateFormat.replaceAll("%orderId%", orderId);
        WATemplateFormat = WATemplateFormat.replaceAll("%storeCity%", storeCity);
        WATemplateFormat = WATemplateFormat.replaceAll("%invoicePdf%", invoicePdf);
        String[] parameterTypeList = WATemplateFormat.split(";"); 
        for (int x=0;x<parameterTypeList.length;x++) {
            String[] temp=parameterTypeList[x].split("=");
            if (temp[0].equalsIgnoreCase("body")) {
                String[] parameterList = temp[1].split(",");
                template.setParameters(parameterList);                    
            } else if (temp[0].equalsIgnoreCase("button")) {
                String[] parameterList = temp[1].split(",");
                template.setParametersButton(parameterList);                    
            } else if (temp[0].equalsIgnoreCase("document")) {
                String parameterDoc = temp[1];
                template.setParametersDocument(parameterDoc); 
                template.setParametersDocumentFileName(invoiceNo+".pdf");
            }
        }
        request.setTemplate(template);
        HttpEntity<WhatsappMessage> httpEntity = new HttpEntity<>(request, headers);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceUrl, "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");
        
        try {
            res = restTemplate.postForEntity(whatsappServiceUrl, httpEntity, String.class);        

            if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
                return true;
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendCustomerAlert res: " + res, "");
                return false;
            }
        
        } catch (Exception ex) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendCustomerAlert res: " + ex.getMessage(), "");
            return false;
        }

    }
    
    
   public boolean sendViewOrderResponse(String[] recipients, Order order, List<OrderItem> orderItems, String orderTime ) throws Exception {
        String logprefix = "sendViewOrderResponse";
        RestTemplate restTemplate = new RestTemplate();        
        HttpHeaders headers = new HttpHeaders();
        
        WhatsappInteractiveMessage request = new WhatsappInteractiveMessage();
        request.setGuest(false);
        request.setRecipientIds(recipients);
        request.setRefId(recipients[0]);
        request.setReferenceId(order.getId());
        request.setOrderId(order.getId());
        
        Interactive  interactiveMsg = GenerateViewOrderMessage(order, orderItems, orderTime);
        request.setInteractive(interactiveMsg);
        
        HttpEntity<WhatsappInteractiveMessage> httpEntity = new HttpEntity<>(request, headers);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceInteractiveUrl, "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");
        
        try {
            ResponseEntity<String> res = restTemplate.postForEntity(whatsappServiceInteractiveUrl, httpEntity, String.class);

            if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
                return true;
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + res, "");
                return false;
            }
        
        } catch (Exception ex) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + ex.getMessage(), "");
            return false;
        }

    }
    
    
    public Interactive GenerateViewOrderMessage(Order order, List<OrderItem> orderItems, String orderTime) {
        
        Interactive interactiveMsg = new Interactive();
        
        String headerText = order.getInvoiceId();
        Header header = new Header();
        header.setType("text");
        header.setText(headerText);
                 
        String bodyText = null;
        String itemList = "";
        int itemCount=1;
        for (OrderItem oi : orderItems) {
            String itemName = "";
            if (oi.getProductVariant()!=null && !"".equals(oi.getProductVariant()) && !"null".equals(oi.getProductVariant())) {
                itemName = oi.getProductName()+" | "+oi.getProductVariant();
            } else if (oi.getOrderSubItem()!=null) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "", "Order subitem size:"+oi.getOrderSubItem().size());
                String subItemList = "";
                for (OrderSubItem subItem : oi.getOrderSubItem()) {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "", "subitem product:"+subItem.getProductName());                
                    if (subItem.getProductName()!=null) {
                        if (subItemList.equals("")) {
                            subItemList = subItem.getProductName();
                        } else {
                            subItemList = subItemList +" | "+subItem.getProductName();
                        }
                    }
                }
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "", "combo item product:"+oi.getProductName());                
                itemName = oi.getProductName()+" | "+subItemList;
            } else{
                itemName = oi.getProductName();
            }
            int quantity = oi.getQuantity();            
            itemList = itemList + itemCount+". " + itemName + " : *[" + quantity + "]*\n";
            itemCount++;
        }
        String currency="RM";
        if (order.getStore().getRegionCountryId().equals("MYS")) {
            currency="RM";
        } else {
            currency="Rs";
        }
        itemList = itemList + "\nTotal Order : *"+currency+Utilities.Round2DecimalPoint(order.getTotal())+"*";  
        itemList = itemList + "\nOrder Date : *"+orderTime+"*";
        itemList = itemList + "\nDelivery Type : *"+ConvertDeliveryType(order)+"*";
        itemList = itemList + "\nCustomer : *"+ConvertCustomerInfo(order)+"*";
        if (order.getCustomerNotes()!=null) {
            itemList = itemList + "\nCustomerNotes : *"+ConvertCustomerNotes(order.getCustomerNotes())+"*";
        }
        bodyText = itemList;
        Body body = new Body();        
        body.setText(bodyText);
        
        List<Button> buttonList = new ArrayList<>();                         
        Button button1 = new Button(new Reply(orderButtonReplyPrefix+"_ORDER_PROCESS,"+order.getId(), "Process Order"));
        Button button2 = new Button(new Reply(orderButtonReplyPrefix+"_ORDER_REJECT,"+order.getId(), "Cancel Order"));
        buttonList.add(button1);
        buttonList.add(button2);
        Action action = new Action();
        action.setButtons(buttonList);
                      
        interactiveMsg.setHeader(header);
        interactiveMsg.setAction(action);
        interactiveMsg.setType("button");
        interactiveMsg.setBody(body);
                
        return interactiveMsg;
    }
    
    public boolean sendNotification(String[] recipients, Order order, String text) throws Exception {
        String logprefix = "sendNotification";
        RestTemplate restTemplate = new RestTemplate();        
        HttpHeaders headers = new HttpHeaders();
        
        WhatsappNotificationMessage request = new WhatsappNotificationMessage();
        request.setGuest(false);
        request.setRecipientIds(recipients);
        request.setRefId(recipients[0]);
        request.setReferenceId(order.getId());
        request.setOrderId(order.getId());
        
        String headerText = order.getInvoiceId();
        Header header = new Header();
        header.setType("text");
        header.setText(headerText);
        
        Body body = new Body();        
        body.setText(text);
        
        Interactive interactiveMsg = new Interactive();
        interactiveMsg.setHeader(header);
        interactiveMsg.setType("button");
        interactiveMsg.setBody(body);
        request.setInteractive(interactiveMsg);
        
        HttpEntity<WhatsappNotificationMessage> httpEntity = new HttpEntity<>(request, headers);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceInteractiveUrl, "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");
        
        try {
            ResponseEntity<String> res = restTemplate.postForEntity(whatsappServiceInteractiveUrl, httpEntity, String.class);

            if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
                return true;
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + res, "");
                return false;
            }
        
        } catch (Exception ex) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + ex.getMessage(), "");
            return false;
        }

    }
    
    
    public boolean sendRetryCancel(String[] recipients, Order order, String text) throws Exception {
        String logprefix = "sendRetryCancel";
        RestTemplate restTemplate = new RestTemplate();        
        HttpHeaders headers = new HttpHeaders();
        
        WhatsappNotificationMessage request = new WhatsappNotificationMessage();
        request.setGuest(false);
        request.setRecipientIds(recipients);
        request.setRefId(recipients[0]);
        request.setReferenceId(order.getId());
        request.setOrderId(order.getId());
        
        String headerText = order.getInvoiceId();
        Header header = new Header();
        header.setType("text");
        header.setText(headerText);
        
        Body body = new Body();        
        body.setText(text);
        
        List<Button> buttonList = new ArrayList<>();                         
        Button button2 = new Button(new Reply(orderButtonReplyPrefix+"_ORDER_REJECT,"+order.getId(), "Cancel Order"));
        buttonList.add(button2);
        Action action = new Action();
        action.setButtons(buttonList);
        
        Interactive interactiveMsg = new Interactive();
        interactiveMsg.setHeader(header);
        interactiveMsg.setAction(action);
        interactiveMsg.setType("button");
        interactiveMsg.setBody(body);
        request.setInteractive(interactiveMsg);
        
        HttpEntity<WhatsappNotificationMessage> httpEntity = new HttpEntity<>(request, headers);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceInteractiveUrl, "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");
        
        try {
            ResponseEntity<String> res = restTemplate.postForEntity(whatsappServiceInteractiveUrl, httpEntity, String.class);

            if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
                return true;
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + res, "");
                return false;
            }
        
        } catch (Exception ex) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + ex.getMessage(), "");
            return false;
        }

    }
    
    
    public boolean sendRetryProcess(String[] recipients, Order order, String text) throws Exception {
        String logprefix = "sendRetryProcess";
        RestTemplate restTemplate = new RestTemplate();        
        HttpHeaders headers = new HttpHeaders();
        
        WhatsappNotificationMessage request = new WhatsappNotificationMessage();
        request.setGuest(false);
        request.setRecipientIds(recipients);
        request.setRefId(recipients[0]);
        request.setReferenceId(order.getId());
        request.setOrderId(order.getId());
        
        String headerText = order.getInvoiceId();
        Header header = new Header();
        header.setType("text");
        header.setText(headerText);
        
        Body body = new Body();        
        body.setText(text);
        
        List<Button> buttonList = new ArrayList<>();                         
        Button button2 = new Button(new Reply(orderButtonReplyPrefix+"_ORDER_PROCESS,"+order.getId(), "Cancel Order"));
        buttonList.add(button2);
        Action action = new Action();
        action.setButtons(buttonList);
        
        Interactive interactiveMsg = new Interactive();
        interactiveMsg.setHeader(header);
        interactiveMsg.setAction(action);
        interactiveMsg.setType("button");
        interactiveMsg.setBody(body);
        
        request.setInteractive(interactiveMsg);
        
        HttpEntity<WhatsappNotificationMessage> httpEntity = new HttpEntity<>(request, headers);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceInteractiveUrl, "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");
        
        try {
            ResponseEntity<String> res = restTemplate.postForEntity(whatsappServiceInteractiveUrl, httpEntity, String.class);

            if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
                return true;
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + res, "");
                return false;
            }
        
        } catch (Exception ex) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + ex.getMessage(), "");
            return false;
        }

    }
    
    public boolean sendProcessOrderResponse(String[] recipients, Order order, String text ) throws Exception {
        String logprefix = "sendProcessOrderResponse";
        RestTemplate restTemplate = new RestTemplate();        
        HttpHeaders headers = new HttpHeaders();
        
        WhatsappInteractiveMessage request = new WhatsappInteractiveMessage();
        request.setGuest(false);
        request.setRecipientIds(recipients);
        request.setRefId(recipients[0]);
        request.setReferenceId(order.getId());
        request.setOrderId(order.getId());
        
        List<Button> buttonList = new ArrayList<>();                         
        Button button2 = new Button(new Reply(orderButtonReplyPrefix+"_ORDER_PICKUP,"+order.getId(), "Ready for Pickup"));
        buttonList.add(button2);
        Action action = new Action();
        action.setButtons(buttonList);
        
        String headerText = order.getInvoiceId();
        Header header = new Header();
        header.setType("text");
        header.setText(headerText);
        
        Body body = new Body();        
        body.setText(text);
        
        Interactive interactiveMsg = new Interactive();
        interactiveMsg.setHeader(header);
        interactiveMsg.setAction(action);
        interactiveMsg.setType("button");
        interactiveMsg.setBody(body);
        request.setInteractive(interactiveMsg);
        
        HttpEntity<WhatsappInteractiveMessage> httpEntity = new HttpEntity<>(request, headers);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceInteractiveUrl, "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");
        
        try {
            ResponseEntity<String> res = restTemplate.postForEntity(whatsappServiceInteractiveUrl, httpEntity, String.class);

            if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
                return true;
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + res, "");
                return false;
            }
        
        } catch (Exception ex) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + ex.getMessage(), "");
            return false;
        }

    }
     
    public boolean sendAwaitingPickupResponse(String[] recipients, Order order, String deliveryLink) throws Exception {
        //alert format : You have new order for store:{{1}} with invoiceNo:{{2}} updated at {{3}}
        String logprefix = "sendAwaitingPickupResponse";
        RestTemplate restTemplate = new RestTemplate();        
        HttpHeaders headers = new HttpHeaders();
        WhatsappMessage request = new WhatsappMessage();
        request.setGuest(false);
        request.setRecipientIds(recipients);
        request.setRefId(recipients[0]);
        request.setReferenceId(orderReminderRefId);
        request.setOrderId(order.getId());
        
        Template template = new Template();
        if (order.getDeliveryType().equalsIgnoreCase("SELF")) {
            template.setName(orderAwaitingSelfPickupTemplateName);
        } else {
            template.setName(orderAwaitingPickupTemplateName);
        }
        String[] message = {order.getInvoiceId(), deliveryLink};
        template.setParameters(message);
        
        String[] headerParam = {order.getInvoiceId()};
        template.setParametersHeader(headerParam);
        
        request.setTemplate(template);
        HttpEntity<WhatsappMessage> httpEntity = new HttpEntity<>(request, headers);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceUrl, "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");
        
        try {
            ResponseEntity<String> res = restTemplate.postForEntity(whatsappServiceUrl, httpEntity, String.class);

            if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
                return true;
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + res, "");
                return false;
            }
        
        } catch (Exception ex) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send sendOrderReminder res: " + ex.getMessage(), "");
            return false;
        }

    }
    
    
    private String ConvertDeliveryType(Order order) {
        if (order.getDeliveryType().equalsIgnoreCase("SELF")) {
            return "SELF-PICKUP";
        } else {
            try {
                String providerName = order.getOrderShipmentDetail().getDeliveryServiceProvider().getName();
                return providerName;
            } catch (Exception ex) {                
            }
            return "DELIVERY";
        }
    }
    
    private String ConvertCustomerInfo(Order order) {
        String customerName = order.getOrderShipmentDetail().getReceiverName();
        String customerContact = order.getOrderShipmentDetail().getPhoneNumber();
        if (customerName.length()>10) {
            customerName = customerName.substring(0,10);
        }
        return customerName + "("+customerContact+")";
    }
    
    private String ConvertCustomerNotes(String notes) {
        if (notes.length()>20) {
            notes = notes.substring(0,20);
        }
        return notes;
    }
}
