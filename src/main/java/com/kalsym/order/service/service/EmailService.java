/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.service;

import com.google.gson.Gson;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import com.kalsym.order.service.model.Email;
import com.kalsym.order.service.model.object.DeliveryServiceResponse;
import com.kalsym.order.service.model.object.DeliveryServiceSubmitOrder;
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

    private static Logger logger = LoggerFactory.getLogger("application");

    //@Autowired
//    @Value("${emailService.SenderAddress:mcmc.lms@gmail.com}")
//    static String emailServiceSenderAddress;
    @Value("${emailService.sendEmail.URL:http://209.58.160.20:2001/email/no-reply/orders}")
    String sendEmailURL;

    public void sendEmail(Email email) {
        logger.info("SendEmail() starting");

        logger.info("mail to :" + Arrays.toString(email.getTo()));
        try {

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
            HttpEntity<Email> httpEntity = new HttpEntity(email, headers);

            logger.info("Sending request to email service : " + email.toString());
            ResponseEntity<String> res = restTemplate.exchange(sendEmailURL, HttpMethod.POST, httpEntity, String.class);

            logger.info("Request sent to email service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                logger.info("EmailServiceResponse:" + res);
            } else {
                logger.info("EmailServiceResponse:" + res);
            }
        } catch (RestClientException ex) {
            logger.error("Exception while sending email to email service: ", ex);
        }

    }
}
