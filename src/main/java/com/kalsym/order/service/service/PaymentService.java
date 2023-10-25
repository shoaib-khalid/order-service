package com.kalsym.order.service.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.EkedaiTransaction;
import com.kalsym.order.service.model.object.MMResponse;
import com.kalsym.order.service.utility.Logger;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;

@Service
public class PaymentService {

    /*
    public MMResponse refundTransaction(EkedaiTransaction transaction) {

        MMResponse mmResponse = new MMResponse();
        String logprefix = "refundTransaction() [" + transaction.getTransactionId() + "]";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("key", OrderServiceApplication.KEY);
        headers.set("Host", OrderServiceApplication.HOST);
        headers.set("User-Agent", "PostmanRuntime/7.33.0");
        String token = getToken(transaction.getTransactionId());
        if (!token.isEmpty()) {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyyMMdd");

            String resultDateStr = sourceFormat.format(transaction.getCreatedDate());

            JsonObject request = new JsonObject();
            request.addProperty("Token", token);
            request.addProperty("OriginalTransactionNumber", transaction.getTransactionId());
            request.addProperty("OriginalTransactionDate", resultDateStr);
            request.addProperty("OriginalAmount", transaction.getTransactionAmount());
            request.addProperty("RefundAmount", transaction.getTransactionAmount());

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request Body", request.toString());
            String url = EkedaiApplication.MMREFUNDURL;
//         Create an HttpEntity with the form data and headers
            HttpEntity<String> requestEntity = new HttpEntity<>(request.toString(), headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            JsonObject jsonResp = new Gson().fromJson(responseEntity.getBody(), JsonObject.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request Url :", url);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Response", jsonResp.toString());

            if (responseEntity.getStatusCodeValue() == 200) {
                mmResponse.setStatus(String.valueOf(responseEntity.getStatusCodeValue()));
                mmResponse.setMessage(jsonResp.get("Message").getAsString());
                mmResponse.setCode(jsonResp.get("Code").getAsString());
            } else {
                mmResponse.setStatus(String.valueOf(responseEntity.getStatusCodeValue()));
                mmResponse.setMessage(jsonResp.get("Message").getAsString());
            }
        }
        return mmResponse;
    }

     */
}
