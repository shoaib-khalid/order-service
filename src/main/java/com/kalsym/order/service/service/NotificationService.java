package com.kalsym.order.service.service;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderNotification;
import com.kalsym.order.service.utility.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Service
public class NotificationService {

    @Autowired
    UserService userService;

    @Value("${hellosim.fcm.send.order.notification.URL:https://mvnouat.hellosim.com.my:9022/hellosim-api/fcm/send-order-notification}")
    String fcmSendOrderNotificationURL;

    @Value("${fcm.push.retry.count:3}")
    int fcmPushRetryCount;

    @Value("${admin.username:admin}")
    String helloSimAdminUsername;

    @Value("${admin.password:111111}")
    String helloSimAdminPassword;

    public void sendOrderNotification(String phoneNumber, String title, String content) {
        String logprefix = "sendOrderNotification";
        boolean pushResult = false;
        int retryCount=0;

        String accessToken = userService.getToken(helloSimAdminUsername, helloSimAdminPassword);

        while (!accessToken.isEmpty() && !pushResult && retryCount < fcmPushRetryCount) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Attempt:"+retryCount+1);
            pushResult = attemptPush(phoneNumber, title, content, accessToken);
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

    public boolean attemptPush(String phoneNumber, String title, String content, String accessToken) {
        String logprefix = "sendOrderNotification";
        RestTemplate restTemplate = new RestTemplate();

        OrderNotification orderNotification = new OrderNotification();

        orderNotification.setTitle(title);
        orderNotification.setBody(content);

        if (phoneNumber.startsWith("+6")) {
            phoneNumber = phoneNumber.substring(2);
        } else if (phoneNumber.startsWith("6")) {
            phoneNumber = phoneNumber.substring(1);
        }
        orderNotification.setTo(phoneNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<OrderNotification> entity = new HttpEntity<>(orderNotification, headers);

        String url = fcmSendOrderNotificationURL;
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " url: " + url);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " entity: " + entity);

        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " res: " + res);

        //check the response
        if (res.getStatusCode() == HttpStatus.OK) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res body : " + res.getBody());
        }
        return true;
    }
}
