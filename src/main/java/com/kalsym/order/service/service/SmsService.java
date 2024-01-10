package com.kalsym.order.service.service;


import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.utility.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;



@Service
public class SmsService {

    public String generateMessage(String smsTemplate, String invoiceNo, String storeName, String trackingUrl) {

        smsTemplate = smsTemplate.replaceAll("%invoiceNo%", invoiceNo);
        smsTemplate = smsTemplate.replaceAll("%storeName%", storeName);
        smsTemplate = smsTemplate.replaceAll("%trackingUrl%", trackingUrl);

        return smsTemplate;

    }

    public String sendSms(String destAddr, String message) {
        try {
            // Set up the URL and parameters
            String url = "https://umgotp.hellosim.com.my/api/processMsg.php";
            String cmd = "submitMT";
            String gwMsgId = "201108031EF00FFF";
            String svrMsgId = "999999999";
            String cliMsgId = "IATS123456789";
            String systemId = "hellosim";
            String password = "hellosimmtrade";
            String srcAddr = "22422";
            String dataCoding = "1";

            if (destAddr.startsWith("0")) {
                destAddr = "6" + destAddr;
            }

            // Encode the message parameter
            String encodedMessage = URLEncoder.encode(message, "UTF-8");

            // Construct the full URL with parameters
            String fullUrl = String.format(
                    "%s?Cmd=%s&GwMsgId=%s&SvrMsgId=%s&CliMsgId=%s&SystemId=%s&Password=%s&Message=%s&SrcAddr=%s&DestAddr=%s&DataCoding=%s",
                    url, cmd, gwMsgId, svrMsgId, cliMsgId, systemId, password, encodedMessage, srcAddr, destAddr,
                    dataCoding);

            // Create headers with Content-Type set to application/json
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create an HTTP entity with headers
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Create a RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();

            // Send the GET request
//            ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "fullUrl", fullUrl);

            return message;

//            // Process the response
//            if (response.getStatusCode().is2xxSuccessful()) {
//                return response.getBody();
//            } else {
//                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, "sendHttpGetRequest",
//                    "Request failed. Status code: " + response.getStatusCodeValue());
//                return "Request failed. Status code: " + response.getStatusCodeValue();
//            }
        } catch (UnsupportedEncodingException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, "sendHttpGetRequest",
                    "UnsupportedEncodingException " + e.getMessage());
            return "Error encoding message parameter: " + e.getMessage();
        }
    }

    public String sendCouponUrl(String destAddr, String couponUrl) {

        String message = "E-Kedai : Congratulations! You have received a free coupon! Click on this link to get the QR ";
        try {
            //Set up the URL and parameters
            String url = "https://umgotp.hellosim.com.my/api/processMsg.php";
            String cmd = "submitMT";
            String gwMsgId = "201108031EF00FFF";
            String svrMsgId = "999999999";
            String cliMsgId = "IATS123456789";
            String systemId = "hellosim";
            String password = "hellosimmtrade";
            String srcAddr = "22422";
            String dataCoding = "1";
            
            if (destAddr.startsWith("0")) {
                destAddr = "6" + destAddr;
            }

            if (couponUrl != null) {
                message = message + couponUrl;

                // Encode the message parameter
                String encodedMessage = URLEncoder.encode(message, "UTF-8");

                // Construct the full URL with parameters
                String fullUrl = String.format(
                "%s?Cmd=%s&GwMsgId=%s&SvrMsgId=%s&CliMsgId=%s&SystemId=%s&Password=%s&Message=%s&SrcAddr=%s&DestAddr=%s&DataCoding=%s",
                url, cmd, gwMsgId, svrMsgId, cliMsgId, systemId, password, encodedMessage, srcAddr, destAddr,
                dataCoding);

                // Create headers with Content-Type set to application/json
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // Create an HTTP entity with headers
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                //Create a RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Send the GET request
                ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, String.class);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "fullUrl", fullUrl);


                // Process the response
                if (response.getStatusCode().is2xxSuccessful()) {
                    return response.getBody();
                } else {
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, "sendHttpGetRequest",
                        "Request failed. Status code: " + response.getStatusCodeValue());
                    return "Request failed. Status code: " + response.getStatusCodeValue();
                }

            } else {
                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, "SendCouponSMS",
                    "Coupon url is not provided!");
                return "Error: Coupon url is not provided";
            }
            
        } catch (UnsupportedEncodingException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, "sendHttpGetRequest",
                    "UnsupportedEncodingException " + e.getMessage());
            return "Error encoding message parameter: " + e.getMessage();
        }

    }

}
