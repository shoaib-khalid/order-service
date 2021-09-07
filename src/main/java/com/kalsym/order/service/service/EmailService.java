/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.service;

import com.kalsym.order.service.OrderServiceApplication;
import org.springframework.beans.factory.annotation.Value;
import com.kalsym.order.service.model.Email;
import com.kalsym.order.service.utility.Logger;
import java.util.Arrays;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author user
 */
@org.springframework.stereotype.Service
public class EmailService {

    //@Autowired
//    @Value("${emailService.SenderAddress:mcmc.lms@gmail.com}")
//    static String emailServiceSenderAddress;
    @Value("${emailService.sendEmail.URL:http://209.58.160.20:2001/email/no-reply/orders}")
    String sendEmailURL;

    public void sendEmail(Email email) {
        String logprefix = "sendEmail";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "starting");

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "mail to :" + Arrays.toString(email.getTo()));
        try {

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
            HttpEntity<Email> httpEntity = new HttpEntity(email, headers);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "sendEmailURL : " + sendEmailURL);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity : " + email.toString());
            ResponseEntity<String> res = restTemplate.exchange(sendEmailURL, HttpMethod.POST, httpEntity, String.class);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request sent to email service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "EmailServiceResponse:" + res);
            } else {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "EmailServiceResponse:" + res);
            }
        } catch (RestClientException ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Exception while sending email to email service: ", ex);
        }

    }
}
