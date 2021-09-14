package com.kalsym.order.service.service;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

    @Value("${fcm.token:key=AAAAj5hNRLI:APA91bEBW0gxueP0sjTtvixEb41IK7mZvDxyiSMDalS6ombzXoidlwGmvsagaF520jTxZxxLd1qsX4H-8iSs2qsgqY-rpdLvpTJFOYq0EGj7Mssjno0A7Xwd7nV8pt29HmewypxfaQ65}")
    String fcmToken;

    @Value("${fcm.title:title-prop-not-set}")
    String fcmTitle;

    @Value("${fcm.title:title-prop-not-setbody-prop-not-set}")
    String fcmBody;

    public void sendPushNotification(Order order, String storeId, String storeName, OrderStatus status) {
        String logprefix = "sendPushNotification";
        RestTemplate restTemplate = new RestTemplate();

        FCMNotification fcmNotification = new FCMNotification();

        fcmNotification.setTo("/topics/" + storeId);
        FCMNotificationData fcmNotificationData = new FCMNotificationData();
        fcmNotificationData.setTitle(fcmTitle);
        fcmNotificationData.setStoreName(storeName);
        String body = fcmBody.replace("$%storeName$%", storeName);
        fcmNotificationData.setBody(body);
        fcmNotification.setData(fcmNotificationData);

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
