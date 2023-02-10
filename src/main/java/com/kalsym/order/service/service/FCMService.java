package com.kalsym.order.service.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.Product;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author saros
 */
@Service
public class FCMService {

    @Value("${fcm.url:https://fcm.googleapis.com/fcm/send}")
    String fcmUrl;

    @Value("${fcm.token.deliverin:key=AAAAj5hNRLI:APA91bEBW0gxueP0sjTtvixEb41IK7mZvDxyiSMDalS6ombzXoidlwGmvsagaF520jTxZxxLd1qsX4H-8iSs2qsgqY-rpdLvpTJFOYq0EGj7Mssjno0A7Xwd7nV8pt29HmewypxfaQ65}")
    String fcmTokenDeliverIn;
    
    @Value("${fcm.token.easydukan:key=AAAAj5hNRLI:APA91bEBW0gxueP0sjTtvixEb41IK7mZvDxyiSMDalS6ombzXoidlwGmvsagaF520jTxZxxLd1qsX4H-8iSs2qsgqY-rpdLvpTJFOYq0EGj7Mssjno0A7Xwd7nV8pt29HmewypxfaQ65}")
    String fcmTokenEasyDukan;
    
    @Value("${fcm.push.retry.count:3}")
    int fcmPushRetryCount;
    
    public void sendPushNotification(Order order, String storeId, String storeName, String title, String content, OrderStatus orderStatus, String domain) {
        String logprefix = "sendPushNotification";
        boolean pushResult = false;
        int retryCount=0;
        while (!pushResult && retryCount < fcmPushRetryCount) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Attempt:"+retryCount+1);
            pushResult = attemptPush(order, storeId, storeName, title, content, orderStatus, domain);
            retryCount++;
            int sleepTime = retryCount * 2 * 1000;
            if (!pushResult) {
                try {
                    Thread.sleep(sleepTime);
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Sleep "+sleepTime+" MS");
                } catch (Exception ex){}            
            }
        }
    }
    
    public boolean attemptPush(Order order, String storeId, String storeName, String title, String content, OrderStatus orderStatus, String domain) {
        String logprefix = "sendPushNotification";
        RestTemplate restTemplate = new RestTemplate();

        FCMNotification fcmNotification = new FCMNotification();

        fcmNotification.setTo("/topics/" + storeId);
        fcmNotification.setPriority("high");
        FCMNotificationData fcmNotificationData = new FCMNotificationData();
        fcmNotificationData.setTitle(title);
        fcmNotificationData.setStoreName(storeName);
        String body = content.replace("$%storeName$%", storeName);
        body = body.replace("$%orderStatus$%", orderStatus.toString());
        body = body.replace("$%invoiceNo$%", order.getInvoiceId());
        body = body.replace("$%orderId%", order.getId());
        fcmNotificationData.setBody(body);
        fcmNotification.setData(fcmNotificationData);
        
        String fcmToken = fcmTokenDeliverIn;
        if (domain.contains("deliverin")) {
            fcmToken = fcmTokenDeliverIn;
        } else if (domain.contains("easydukan")) {
            fcmToken = fcmTokenEasyDukan;
        } else if (domain.contains("dev-my")) {
            fcmToken = fcmTokenDeliverIn;
        } else if (domain.contains("dev-pk")) {
            fcmToken = fcmTokenEasyDukan;
        } else {
            fcmToken = fcmTokenDeliverIn;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", fcmToken);

        HttpEntity<FCMNotification> entity = new HttpEntity<>(fcmNotification, headers);

        String url = fcmUrl;
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " url: " + url);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " entity: " + entity);

        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " res: " + res);
        
        //check the response
        if (res.getStatusCode() == HttpStatus.OK) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res body : " + res.getBody());
            
            JSONObject jsonObject = new JSONObject(res.getBody());
            
            String error = null;
            try {
                error = jsonObject.get("error").toString();
            } catch (Exception ex) {  
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "error not found : "+ex.getMessage());
            }
            
            error = "TOPICS_MESSAGE_RATE_EXCEEDED";
            
            String message_id = null;
            try {
                message_id = jsonObject.get("message_id").toString();
            } catch (Exception ex) {                
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "message_id not found : "+ex.getMessage());
            }
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "MessageId:"+message_id+" Error:"+error); 
            
            if (error!=null && error.equalsIgnoreCase("TOPICS_MESSAGE_RATE_EXCEEDED")) {
                return false;
            }
        }
        
        return true;

    }
    
    
    public void sendQrcodeNotification(String storeId, String storeName, String domain) {
        String logprefix = "sendQrcodeNotification";
        RestTemplate restTemplate = new RestTemplate();

        FCMNotification fcmNotification = new FCMNotification();

        fcmNotification.setTo("/topics/" + storeId);
        FCMNotificationData fcmNotificationData = new FCMNotificationData();
        fcmNotificationData.setTitle("qrcode");
        fcmNotificationData.setStoreName(storeName);
        String body = "Please request new qrcode";
        fcmNotificationData.setBody(body);
        fcmNotification.setData(fcmNotificationData);
        
        String fcmToken = fcmTokenDeliverIn;
        if (domain.contains("deliverin")) {
            fcmToken = fcmTokenDeliverIn;
        } else if (domain.contains("easydukan")) {
            fcmToken = fcmTokenEasyDukan;
        } else if (domain.contains("dev-my")) {
            fcmToken = fcmTokenDeliverIn;
        } else if (domain.contains("dev-pk")) {
            fcmToken = fcmTokenEasyDukan;
        } else {
            fcmToken = fcmTokenDeliverIn;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", fcmToken);

        HttpEntity<FCMNotification> entity = new HttpEntity<>(fcmNotification, headers);

        String url = fcmUrl;
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " url: " + url);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " entity: " + entity);

        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " res: " + res);

    }

}
